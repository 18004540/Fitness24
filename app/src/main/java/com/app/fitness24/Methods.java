package com.app.fitness24;

public class Methods {
    public static double convertKgToPounds(double v) {
        return v * 2.205;
    }

    public static double convertPoundsToKg(double v) {
        return v / 2.205;
    }

    public static double convertMeterToInches(double v) {
        return v * 39.37;
    }

    public static double convertInchesToMeter(double v) {
        return v / 39.37;
    }
}
