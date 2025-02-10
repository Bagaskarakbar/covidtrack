package com.fadhlillahb.covidtracker.wear;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.samsung.android.service.health.tracking.ConnectionListener;
import com.samsung.android.service.health.tracking.HealthTrackerException;
import com.samsung.android.service.health.tracking.HealthTrackingService;
import com.samsung.android.service.health.tracking.data.HealthTrackerType;
import com.fadhlillahb.covidtracker.R;

import java.util.List;

import androidx.annotation.NonNull;

public class ConnectionManager {
    private final static String TAG = "Connection Manager";
    private final ConnectionObserver connectionObserver;
    private HealthTrackingService healthTrackingService = null;
    private final ConnectionListener connectionListener = new ConnectionListener() {
        @Override
        public void onConnectionSuccess() {
            Log.i(TAG, "Connected");
            connectionObserver.onConnectionResult(R.string.ConnectedToHs);
            if (!isSpO2Available(healthTrackingService)) {
                Log.i(TAG, "Device does not support SpO2 tracking");
                connectionObserver.onConnectionResult(R.string.NoSpo2Support);
            }
            if (!isHeartRateAvailable(healthTrackingService)) {
                Log.i(TAG, "Device does not support Heart Rate tracking");
                connectionObserver.onConnectionResult(R.string.NoHrSupport);
            }
            if (!isSkinTempAvailable(healthTrackingService)) {
                Log.i(TAG, "Device does not support Skin Temp tracking");
                connectionObserver.onConnectionResult(R.string.NoTempSupport);
            }
        }

        @Override
        public void onConnectionEnded() {
            Log.i(TAG, "Disconnected");
        }

        @Override
        public void onConnectionFailed(HealthTrackerException e) {
            connectionObserver.onError(e);
        }
    };

    ConnectionManager(ConnectionObserver observer) {
        connectionObserver = observer;
    }

    public void connect(Context context) {
        healthTrackingService = new HealthTrackingService(connectionListener, context);
        healthTrackingService.connectService();
    }

    public void disconnect() {
        if (healthTrackingService != null)
            healthTrackingService.disconnectService();
    }

    public void initSpO2(SpO2Listener spO2Listener) {
        spO2Listener.setHealthTracker(healthTrackingService.getHealthTracker(HealthTrackerType.SPO2_ON_DEMAND));
        setHandlerForBaseListener(spO2Listener);
    }

    public void initHeartRate(HeartRateListener heartRateListener) {
        heartRateListener.setHealthTracker(healthTrackingService.getHealthTracker(HealthTrackerType.HEART_RATE_CONTINUOUS));
        setHandlerForBaseListener(heartRateListener);
    }

    public void initTemp(TempListener tempListener) {
        tempListener.setHealthTracker(healthTrackingService.getHealthTracker(HealthTrackerType.SKIN_TEMPERATURE_CONTINUOUS));
        setHandlerForBaseListener(tempListener);
    }

    private void setHandlerForBaseListener(BaseListener baseListener) {
        baseListener.setHandler(new Handler(Looper.getMainLooper()));
    }

    private boolean isSpO2Available(@NonNull HealthTrackingService healthTrackingService) {
        final List<HealthTrackerType> availableTrackers = healthTrackingService.getTrackingCapability().getSupportHealthTrackerTypes();
        return availableTrackers.contains(HealthTrackerType.SPO2_ON_DEMAND);
    }

    private boolean isHeartRateAvailable(@NonNull HealthTrackingService healthTrackingService) {
        final List<HealthTrackerType> availableTrackers = healthTrackingService.getTrackingCapability().getSupportHealthTrackerTypes();
        return availableTrackers.contains(HealthTrackerType.HEART_RATE_CONTINUOUS);
    }

    private boolean isSkinTempAvailable(@NonNull HealthTrackingService healthTrackingService) {
        final List<HealthTrackerType> availableTrackers = healthTrackingService.getTrackingCapability().getSupportHealthTrackerTypes();
        return availableTrackers.contains(HealthTrackerType.SKIN_TEMPERATURE_CONTINUOUS);
    }


}
