package com.fadhlillahb.covidtracker.wear;

import android.util.Log;

import com.fadhlillahb.covidtracker.R;
import com.samsung.android.service.health.tracking.HealthTracker;
import com.samsung.android.service.health.tracking.data.DataPoint;
import com.samsung.android.service.health.tracking.data.ValueKey;

import java.util.List;

import androidx.annotation.NonNull;

public class TempListener extends BaseListener{
    private final static String APP_TAG = "TempListener";

    TempListener() {
        final HealthTracker.TrackerEventListener trackerEventListener = new HealthTracker.TrackerEventListener() {
            @Override
            public void onDataReceived(@NonNull List<DataPoint> list) {
                for (DataPoint dataPoint : list) {
                    readValuesFromDataPoint(dataPoint);
                }
            }

            @Override
            public void onFlushCompleted() {
                Log.i(APP_TAG, " onFlushCompleted called");
            }

            @Override
            public void onError(HealthTracker.TrackerError trackerError) {
                Log.e(APP_TAG, " onError called: " + trackerError);
                setHandlerRunning(false);
                if (trackerError == HealthTracker.TrackerError.PERMISSION_ERROR) {
                    TrackerDataNotifier.getInstance().notifyError(R.string.NoPermission);
                }
                if (trackerError == HealthTracker.TrackerError.SDK_POLICY_ERROR) {
                    TrackerDataNotifier.getInstance().notifyError(R.string.SdkPolicyError);
                }
            }
        };
        setTrackerEventListener(trackerEventListener);
    }

    public void readValuesFromDataPoint(DataPoint dataPoint) {
        final TempData tempData = new TempData();
        tempData.status = dataPoint.getValue(ValueKey.SkinTemperatureSet.STATUS);
        tempData.wristSkinTemperature = dataPoint.getValue(ValueKey.SkinTemperatureSet.OBJECT_TEMPERATURE);
        tempData.ambientTemperature = dataPoint.getValue(ValueKey.SkinTemperatureSet.AMBIENT_TEMPERATURE);
        TrackerDataNotifier.getInstance().notifySkinTemperatureTrackerObservers(tempData);
        Log.d(APP_TAG, dataPoint.toString());
    }
}
