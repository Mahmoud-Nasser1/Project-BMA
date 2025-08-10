package com.banking;

import javafx.scene.paint.Color;
public class PinData {
    private final double x;
    private final double y;
    private final String country;
    private final double rotation;

    public PinData(double x, double y, String country, double rotation) {
        this.x = x;
        this.y = y;
        this.country = country;
        this.rotation = rotation;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public String getCountry() { return country; }
    public double getRotation() { return rotation; }
}
