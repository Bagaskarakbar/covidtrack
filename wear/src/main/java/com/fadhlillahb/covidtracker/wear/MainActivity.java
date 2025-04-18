package com.fadhlillahb.covidtracker.wear;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import com.fadhlillahb.covidtracker.R;
import com.fadhlillahb.covidtracker.databinding.ActivityMainBinding;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.samsung.android.service.health.tracking.HealthTrackerException;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import static android.content.pm.PackageManager.PERMISSION_DENIED;

public class MainActivity extends Activity{
    private final static String APP_TAG = "MainActivity";
    private final static int MEASUREMENT_DURATION = 35000;
    private final static Long MEASUREMENT_TICK = 250L;
    private static final String DATA_PATH = "/data_path"; // Define a specific path for heart rate
    private static final String KEY_TEMP = "key_temp";
    private static final String KEY_SPO = "key_spo";
    private static final String KEY_HR = "key_hr";

    private final AtomicBoolean isMeasurementRunning = new AtomicBoolean(false);
    Thread uiUpdateThread = null;
    private ConnectionManager connectionManager;
    private HeartRateListener heartRateListener = null;
    private TempListener tempListener = null;
    private SpO2Listener spO2Listener = null;
    private boolean connected = false;
    private boolean permissionGranted = false;
    private int previousStatus = SpO2Status.INITIAL_STATUS;
    private HeartRateData heartRateDataLast = new HeartRateData();
    private int spo2DataLast = 0;
    private TempData tempDataLast = new TempData();
    private TextView txtHeartRate;
    private TextView txtStatus;
    private TextView txtSpo2;
    private TextView txtTemp;
    private Button butStart;
    private CircularProgressIndicator measurementProgress = null;
    final CountDownTimer countDownTimer = new CountDownTimer(MEASUREMENT_DURATION, MEASUREMENT_TICK) {
        @Override
        public void onTick(long timeLeft) {
            if (isMeasurementRunning.get()) {
                runOnUiThread(() ->
                        measurementProgress.setProgress(measurementProgress.getProgress() + 1, true));
            } else
                cancel();
        }

        @Override
        public void onFinish() {
            if (!isMeasurementRunning.get())
                return;
            Log.i(APP_TAG, "Failed measurement");
            spO2Listener.stopTracker();
            isMeasurementRunning.set(false);
            runOnUiThread(() ->
            {
                txtStatus.setText(R.string.MeasurementFailed);
                txtStatus.invalidate();
                txtSpo2.setText(R.string.SpO2DefaultValue);
                txtSpo2.invalidate();
                butStart.setText(R.string.StartLabel);
                measurementProgress.setProgress(measurementProgress.getMax(), true);
                measurementProgress.invalidate();
            });
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    };
    final TrackerDataObserver trackerDataObserver = new TrackerDataObserver() {
        @Override
        public void onHeartRateTrackerDataChanged(HeartRateData hrData) {
            MainActivity.this.runOnUiThread(() -> {
                heartRateDataLast = hrData;
                Log.i(APP_TAG, "HR Status: " + hrData.status);
                if (hrData.status == HeartRateStatus.HR_STATUS_FIND_HR) {
                    txtHeartRate.setText(String.valueOf(hrData.hr));
                    Log.i(APP_TAG, "HR: " + hrData.hr);
                } else {
                    txtHeartRate.setText(getString(R.string.HeartRateDefaultValue));
                }
            });
        }

        @Override
        public void onSpO2TrackerDataChanged(int status, int spO2Value) {
            if(status == previousStatus) {
                return;
            }
            previousStatus = status;
            switch (status) {
                case SpO2Status.CALCULATING:
                    Log.i(APP_TAG, "Calculating measurement");
                    runOnUiThread(() -> {
                                txtStatus.setText(R.string.StatusCalculating);
                                txtStatus.invalidate();
                            }
                    );
                    break;
                case SpO2Status.DEVICE_MOVING:
                    Log.i(APP_TAG, "Device is moving");
                    runOnUiThread(() ->
                            Toast.makeText(getApplicationContext(), R.string.StatusDeviceMoving, Toast.LENGTH_SHORT).show());
                    break;
                case SpO2Status.LOW_SIGNAL:
                    Log.i(APP_TAG, "Low signal quality");
                    runOnUiThread(() ->
                            Toast.makeText(getApplicationContext(), R.string.StatusLowSignal, Toast.LENGTH_SHORT).show());
                    break;
                case SpO2Status.MEASUREMENT_COMPLETED:
                    Log.i(APP_TAG, "Measurement completed");
                    isMeasurementRunning.set(false);
                    spO2Listener.stopTracker();
                    runOnUiThread(() -> {
                        txtStatus.setText(R.string.StatusCompleted);
                        txtStatus.invalidate();
                        spo2DataLast = spO2Value;
                        txtSpo2.setText(String.valueOf(spO2Value));
                        txtSpo2.invalidate();
                        butStart.setText(R.string.StartLabel);
                        measurementProgress.setProgress(measurementProgress.getMax(), true);
                    });
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    break;
            }
        }

        @Override
        public void onSkinTemperatureChanged(TempData tempData) {
            MainActivity.this.runOnUiThread(() -> {
                tempDataLast = tempData;
                Log.i(APP_TAG, "Temp Status: " + tempData.status);
                if (tempData.status == TempStatus.SUCCESSFUL_MEASUREMENT) {
                    txtTemp.setText(String.format(Locale.getDefault(), "%.1f",tempData.wristSkinTemperature));
                    Log.i(APP_TAG, "Temp: " + String.format(Locale.getDefault(), "%.1f",tempData.wristSkinTemperature));
                } else {
                    txtTemp.setText(getString(R.string.HeartRateDefaultValue));
                }
            });
        }

        @Override
        public void onError(int errorResourceId) {
            runOnUiThread(() ->
                    Toast.makeText(getApplicationContext(), getString(errorResourceId), Toast.LENGTH_LONG).show());
            countDownTimer.onFinish();
        }
    };
    private final ConnectionObserver connectionObserver = new ConnectionObserver() {
        @Override
        public void onConnectionResult(int stringResourceId) {
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), getString(stringResourceId)
                    , Toast.LENGTH_LONG).show());

            if (stringResourceId != R.string.ConnectedToHs) {
                finish();
            }

            connected = true;
            TrackerDataNotifier.getInstance().addObserver(trackerDataObserver);

            spO2Listener = new SpO2Listener();
            heartRateListener = new HeartRateListener();
            tempListener = new TempListener();

            connectionManager.initSpO2(spO2Listener);
            connectionManager.initHeartRate(heartRateListener);
            connectionManager.initTemp(tempListener);

            heartRateListener.startTracker();
            tempListener.startTracker();
        }

        @Override
        public void onError(HealthTrackerException e) {
            if (e.getErrorCode() == HealthTrackerException.OLD_PLATFORM_VERSION || e.getErrorCode() == HealthTrackerException.PACKAGE_NOT_INSTALLED)
                runOnUiThread(() -> Toast.makeText(getApplicationContext()
                        , getString(R.string.HealthPlatformVersionIsOutdated), Toast.LENGTH_LONG).show());
            if (e.hasResolution()) {
                e.resolve(MainActivity.this);
            } else {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), getString(R.string.ConnectionError)
                        , Toast.LENGTH_LONG).show());
                Log.e(APP_TAG, "Could not connect to Health Tracking Service: " + e.getMessage());
            }
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        txtHeartRate = binding.txtHeartRate;
        txtStatus = binding.txtStatus;
        txtSpo2 = binding.txtSpO2;
        txtTemp = binding.txtTemp;
        butStart = binding.butStart;
        measurementProgress = binding.progressBar;
        adjustProgressBar(measurementProgress);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), getString(R.string.BodySensors)) == PackageManager.PERMISSION_DENIED)
            requestPermissions(new String[]{Manifest.permission.BODY_SENSORS}, 0);
        else {
            permissionGranted = true;
            createConnectionManager();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (heartRateListener != null)
            heartRateListener.stopTracker();
        if (tempListener != null)
            tempListener.stopTracker();
        if (spO2Listener != null)
            spO2Listener.stopTracker();
        TrackerDataNotifier.getInstance().removeObserver(trackerDataObserver);
        if (connectionManager != null) {
            connectionManager.disconnect();
        }
    }

    void createConnectionManager() {
        try {
            connectionManager = new ConnectionManager(connectionObserver);
            connectionManager.connect(getApplicationContext());

        } catch (Throwable t) {
            Log.e(APP_TAG, t.getMessage());
        }
    }

    void adjustProgressBar(CircularProgressIndicator progressBar) {
        final DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        final int pxWidth = displayMetrics.widthPixels;
        final int padding = 1;
        progressBar.setPadding(padding, padding, padding, padding);
        final int trackThickness = progressBar.getTrackThickness();

        final int progressBarSize = pxWidth - trackThickness - 2 * padding;
        progressBar.setIndicatorSize(progressBarSize);
    }

    public void onDetails(View view) {
        if (isPermissionsOrConnectionInvalid()) {
            return;
        }

        final Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(getString(R.string.ExtraHr), heartRateDataLast.hr);
        intent.putExtra(getString(R.string.ExtraHrStatus), heartRateDataLast.status);
        intent.putExtra(getString(R.string.ExtraIbi), heartRateDataLast.ibi);
        intent.putExtra(getString(R.string.ExtraQualityIbi), heartRateDataLast.qIbi);
        startActivity(intent);
    }

    public void performMeasurement(View view) {
        if (isPermissionsOrConnectionInvalid()) {
            return;
        }

        if (!isMeasurementRunning.get()) {
            previousStatus = SpO2Status.INITIAL_STATUS;
            butStart.setText(R.string.StopLabel);
            txtSpo2.setText(R.string.SpO2DefaultValue);
            measurementProgress.setProgress(0);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            spO2Listener.startTracker();
            isMeasurementRunning.set(true);
            uiUpdateThread = new Thread(countDownTimer::start);
            uiUpdateThread.start();
        } else {
            butStart.setEnabled(false);
            isMeasurementRunning.set(false);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            spO2Listener.stopTracker();
            final Handler progressHandler = new Handler(Looper.getMainLooper());
            progressHandler.postDelayed(() ->
                    {
                        butStart.setText(R.string.StartLabel);
                        txtStatus.setText(R.string.StatusDefaultValue);
                        measurementProgress.setProgress(0);
                        butStart.setEnabled(true);
                    }, MEASUREMENT_TICK * 2
            );
        }
    }

    public void performSend (View view){
        PutDataMapRequest dataMapRequest = PutDataMapRequest.create(DATA_PATH);
        DataMap dataMap = dataMapRequest.getDataMap();
        HeartRateData heartratedata = heartRateDataLast;
        TempData tempData = tempDataLast;
        int spo2 = spo2DataLast;

        //datas
        dataMap.putInt(KEY_HR, heartratedata.hr);
        dataMap.putInt(KEY_SPO, spo2);
        dataMap.putFloat(KEY_TEMP, tempDataLast.wristSkinTemperature);

        Wearable.getDataClient(this).putDataItem(dataMapRequest.asPutDataRequest())
                .addOnSuccessListener(dataItem -> Log.d("DATACLIENTSENT", "the datas are : " + dataMap.getInt(KEY_HR) + " " + dataMap.getInt(KEY_SPO) + " " + dataMap.getFloat(KEY_TEMP)))
                .addOnFailureListener(e -> Log.e("WearMainActivity", "Failed to send heart rate", e));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            permissionGranted = true;
            for (int i = 0; i < permissions.length; ++i) {
                if (grantResults[i] == PERMISSION_DENIED) {
                    //User denied permissions twice - permanent denial:
                    if (!shouldShowRequestPermissionRationale(permissions[i]))
                        Toast.makeText(getApplicationContext(), getString(R.string.PermissionDeniedPermanently), Toast.LENGTH_LONG).show();
                        //User denied permissions once:
                    else
                        Toast.makeText(getApplicationContext(), getString(R.string.PermissionDeniedRationale), Toast.LENGTH_LONG).show();
                    permissionGranted = false;
                    break;
                }
            }
            if (permissionGranted) {
                createConnectionManager();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean isPermissionsOrConnectionInvalid() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), getString(R.string.BodySensors)) == PackageManager.PERMISSION_DENIED)
            requestPermissions(new String[]{Manifest.permission.BODY_SENSORS}, 0);
        if (!permissionGranted) {
            Log.i(APP_TAG, "Could not get permissions. Terminating measurement");
            return true;
        }
        if (!connected) {
            Toast.makeText(getApplicationContext(), getString(R.string.ConnectionError), Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }
}
