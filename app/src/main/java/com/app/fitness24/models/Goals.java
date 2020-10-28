package com.app.fitness24.models;

public class Goals {
    String date, weight, calorie;

    public Goals() {
    }

    public Goals(String date, String weight, String calorie) {
        this.date = date;
        this.weight = weight;
        this.calorie = calorie;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getCalorie() {
        return calorie;
    }

    public void setCalorie(String calorie) {
        this.calorie = calorie;
    }
}
