package com.fadhlillahb.covidtracker.wear;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.fadhlillahb.covidtracker.R;
import com.fadhlillahb.covidtracker.databinding.ActivityDetailsBinding;

import androidx.fragment.app.FragmentActivity;

public class DetailsActivity extends FragmentActivity {
    private final String APP_TAG = "DetailsActivity";

    TextView txtStatus;
    TextView txtHeartRate;
    TextView txtHeartRateStatus;
    TextView txtIbi;
    TextView txtIbiStatus;
    final TrackerDataObserver trackerDataObserver = new TrackerDataObserver() {
        @Override
        public void onHeartRateTrackerDataChanged(HeartRateData hrData) {
            DetailsActivity.this.runOnUiThread(() -> updateUi(hrData));
        }

        @Override
        public void onSpO2TrackerDataChanged(int status, int spO2Value) {
        }

        public void onSkinTemperatureChanged(TempData tempData){
        }

        @Override
        public void onError(int errorResourceId) {
            runOnUiThread(() ->
                    Toast.makeText(getApplicationContext(), getString(errorResourceId), Toast.LENGTH_LONG));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityDetailsBinding binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //setContentView(R.layout.activity_details);

        txtStatus = binding.txtStatus;
        txtHeartRate = binding.txtHeartRate;
        txtHeartRateStatus = binding.txtHeartRateStatus;
        txtIbi = binding.txtIbi;
        txtIbiStatus = binding.txtIbiStatus;

        final Intent intent = getIntent();
        final int status = intent.getIntExtra(getString(R.string.ExtraHrStatus), HeartRateStatus.HR_STATUS_NONE);
        final int hr = intent.getIntExtra(getString(R.string.ExtraHr), 0);
        final int ibi = intent.getIntExtra(getString(R.string.ExtraIbi), 0);
        final int qIbi = intent.getIntExtra(getString(R.string.ExtraQualityIbi), 1);

        final HeartRateData hrData = new HeartRateData(status, hr, ibi, qIbi);
        updateUi(hrData);

        TrackerDataNotifier.getInstance().addObserver(trackerDataObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TrackerDataNotifier.getInstance().removeObserver(trackerDataObserver);
    }

    private void updateUi(HeartRateData hrData) {
        txtHeartRateStatus.setText(String.valueOf(hrData.status));
        setStatus(hrData.status);

        if (hrData.status == HeartRateStatus.HR_STATUS_FIND_HR) {
            txtHeartRate.setText(String.valueOf(hrData.hr));
            txtHeartRateStatus.setTextColor(Color.WHITE);

            txtIbi.setText(String.valueOf(hrData.ibi));
            txtIbiStatus.setText(String.valueOf(hrData.qIbi));
            txtIbiStatus.setTextColor((hrData.qIbi == 0) ? Color.WHITE : Color.RED);
            Log.d(APP_TAG, "HR : " + hrData.hr + " HR_IBI : " + hrData.ibi + "(" + hrData.qIbi + ") ");

        } else {
            txtHeartRate.setText(getString(R.string.HeartRateDefaultValue));
            txtHeartRateStatus.setTextColor(Color.RED);
            txtIbi.setText(getString(R.string.IbiDefaultValue));
            txtIbiStatus.setText(getString(R.string.IbiStatusDefaultValue));
            txtIbiStatus.setTextColor(Color.RED);
        }
    }

    private void setStatus(int status) {
        Log.i(APP_TAG, "HR Status: " + status);
        int stringId = R.string.DetailsStatusNone;
        switch (status) {
            case HeartRateStatus.HR_STATUS_FIND_HR:
                stringId = R.string.DetailsStatusFindHr;
                break;
            case HeartRateStatus.HR_STATUS_NONE:
                break;
            case HeartRateStatus.HR_STATUS_ATTACHED:
                stringId = R.string.DetailsStatusAttached;
                break;
            case HeartRateStatus.HR_STATUS_DETECT_MOVE:
                stringId = R.string.DetailsStatusMoveDetection;
                break;
            case HeartRateStatus.HR_STATUS_DETACHED:
                stringId = R.string.DetailsStatusDetached;
                break;
            case HeartRateStatus.HR_STATUS_LOW_RELIABILITY:
                stringId = R.string.DetailsStatusLowReliability;
                break;
            case HeartRateStatus.HR_STATUS_VERY_LOW_RELIABILITY:
                stringId = R.string.DetailsStatusVeryLowReliability;
                break;
            case HeartRateStatus.HR_STATUS_NO_DATA_FLUSH:
                stringId = R.string.DetailsStatusNoDataFlush;
                break;
        }

        txtStatus.setText(getString(stringId));
    }
}
