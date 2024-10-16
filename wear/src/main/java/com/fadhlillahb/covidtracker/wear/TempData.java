package com.fadhlillahb.covidtracker.wear;

public class TempData {

    int status = TempStatus.INITIAL_STATUS;
    float ambientTemperature = 0;
    float wristSkinTemperature = 0;


    public TempData() {

    }

    public TempData(int status, float ambientTemperature, float wristSkinTemperature) {
        this.status = status;
        this.ambientTemperature = ambientTemperature;
        this.wristSkinTemperature = wristSkinTemperature;
    }


}
