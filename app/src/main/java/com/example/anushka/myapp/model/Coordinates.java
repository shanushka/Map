package com.example.anushka.myapp.model;

/**
 * Created by Anushka on 1/31/2018.
 */

public class Coordinates {
    Double lat,lang;
    int vechicleCount;

    public int getVechicleCount() {
        return vechicleCount;
    }

    public void setVechicleCount(int vechicleCount) {
        this.vechicleCount = vechicleCount;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLang() {
        return lang;
    }

    public void setLang(Double lang) {
        this.lang = lang;
    }
}
