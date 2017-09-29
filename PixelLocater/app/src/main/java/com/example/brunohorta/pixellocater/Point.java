package com.example.brunohorta.pixellocater;

/**
 * Created by brunohorta on 28/09/2017.
 */

public class Point {
    protected double x;
    protected double y;
    protected  double distance;
    protected String id;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point() {
    }

    public Point(double x, double y, String id) {
        this.x = x;
        this.y = y;
        this.id = id;
    }
}
