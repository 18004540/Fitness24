package com.app.fitness24.models;

public class Setting {
    boolean isMetric;

    public Setting() {

    }

    public Setting(boolean isMetric) {
        this.isMetric = isMetric;
    }

    public boolean isMetric() {
        return isMetric;
    }

    public void setMetric(boolean metric) {
        isMetric = metric;
    }
}
