package com.app.fitness24.models;

public class Profile {
    boolean isMetric;
    double height, weight;

    public Profile() {

    }

    public Profile(boolean isMetric, double height, double weight) {
        this.isMetric = isMetric;
        this.height = height;
        this.weight = weight;
    }

    public boolean isMetric() {
        return isMetric;
    }

    public void setMetric(boolean metric) {
        isMetric = metric;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "isMetric=" + isMetric +
                ", height=" + height +
                ", weight=" + weight +
                '}';
    }
}
