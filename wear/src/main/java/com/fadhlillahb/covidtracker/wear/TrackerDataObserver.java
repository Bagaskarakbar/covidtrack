package com.fadhlillahb.covidtracker.wear;

public interface TrackerDataObserver {
    void onHeartRateTrackerDataChanged(HeartRateData hrData);

    void onSpO2TrackerDataChanged(int status, int spO2Value);

    void onSkinTemperatureChanged(TempData tempData);

    void onError(int errorResourceId);
}
