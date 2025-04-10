package com.fadhlillahb.covidtracker.wear;

import android.os.Handler;
import android.util.Log;

import com.samsung.android.service.health.tracking.HealthTracker;

public class BaseListener {
    private final static String APP_TAG = "BaseListener";

    private Handler handler;
    private HealthTracker healthTracker;
    private boolean isHandlerRunning = false;

    private HealthTracker.TrackerEventListener trackerEventListener = null;

    public void setHealthTracker(HealthTracker tracker) {
        healthTracker = tracker;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setHandlerRunning(boolean handlerRunning) {
        isHandlerRunning = handlerRunning;
    }

    public void setTrackerEventListener(HealthTracker.TrackerEventListener tracker) {
        trackerEventListener = tracker;
    }

    public void startTracker() {
        Log.i(APP_TAG, "startTracker called ");
        Log.d(APP_TAG, "healthTracker: " + healthTracker.toString());
        Log.d(APP_TAG, "trackerEventListener: " + trackerEventListener.toString());
        if (!isHandlerRunning) {
            handler.post(() -> {
                healthTracker.setEventListener(trackerEventListener);
                setHandlerRunning(true);
            });
        }
    }

    /*******************************************************************************************
     * [Practice 4] Stop health tracker by removing event listener
     *  - set health tracker set event listener
     -------------------------------------------------------------------------------------------
     *  - (Hint) Replace TODO 4 with parts of code
     *      unset event listener on HealthTracker object using healthTracker.unsetEventListener()
     ******************************************************************************************/

    public void stopTracker() {
        Log.i(APP_TAG, "stopTracker called ");
        Log.d(APP_TAG, "healthTracker: " + healthTracker.toString());
        Log.d(APP_TAG, "trackerEventListener: " + trackerEventListener.toString());
        if (isHandlerRunning) {
            healthTracker.unsetEventListener();
            setHandlerRunning(false);

            handler.removeCallbacksAndMessages(null);
        }
    }
}
