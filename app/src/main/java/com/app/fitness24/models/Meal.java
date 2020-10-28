package com.app.fitness24.models;

public class Meal {
    String date,image;

    public Meal() {
    }

    public Meal(String date, String image) {
        this.date = date;
        this.image = image;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Meal{" +
                "date='" + date + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
