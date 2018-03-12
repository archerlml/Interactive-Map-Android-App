package com.example.sjsucampus.map;

/**
 * Created by chitoo on 10/22/16.
 */
public class Segment {
    public Point p1;
    public Point p2;

    public Segment(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public double dx() {
        return Math.abs(p1.x - p2.x);
    }

    public double dy() {
        return Math.abs(p1.y - p2.y);
    }

    public double distance() {
        return Calculator.distanceOf2Points(p1, p2);
    }
}
