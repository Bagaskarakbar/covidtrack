package com.fadhlillahb.covidtracker.wear;

import android.util.Log;

import com.samsung.android.service.health.tracking.HealthTracker;
import com.samsung.android.service.health.tracking.data.DataPoint;
import com.samsung.android.service.health.tracking.data.ValueKey;
import com.fadhlillahb.covidtracker.R;

import java.util.List;

import androidx.annotation.NonNull;

public class HeartRateListener extends BaseListener{
    private final static String APP_TAG = "HeartRateListener";

    HeartRateListener() {
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

    /*******************************************************************************************
     * [Practice 6] Read values from DataPoint object
     *  - Get heart rate status
     *  - Get heart rate value
     *  - Get heart rate ibi value
     *  - Check retrieved heart rate’s IBI and IBI quality values
     -------------------------------------------------------------------------------------------
     *  - (Hint) Replace TODO 6 with parts of code
     *      (1) set hrData.status from 'dataPoint' object using dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE_STATUS)
     *      (2) set hrData.hr from 'dataPoint' object using dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE)
     *      (3) set local variable 'List<Integer> hrIbiList' using dataPoint.getValue(ValueKey.HeartRateSet.IBI_LIST)
     *      (4) set local variable 'final List<Integer> hrIbiStatus' using dataPoint.getValue(ValueKey.HeartRateSet.IBI_STATUS_LIST)
     *      (5) set hrData.ibi with the last of 'hrIbiList' values
     *      (6) set hrData.qIbi with the last of 'hrIbiStatus' values
     ******************************************************************************************/

    public void readValuesFromDataPoint(DataPoint dataPoint) {
        final HeartRateData hrData = new HeartRateData();
        hrData.status = dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE_STATUS);
        hrData.hr = dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE);
        List<Integer> hrIbiList = dataPoint.getValue(ValueKey.HeartRateSet.IBI_LIST);
        hrData.ibi = hrIbiList.get(hrIbiList.size()-1);
        List<Integer> hrIbiStatus = dataPoint.getValue(ValueKey.HeartRateSet.IBI_STATUS_LIST);
        hrData.qIbi = hrIbiStatus.get(hrIbiStatus.size()-1);
        TrackerDataNotifier.getInstance().notifyHeartRateTrackerObservers(hrData);
        Log.d(APP_TAG, dataPoint.toString());
    }
}
