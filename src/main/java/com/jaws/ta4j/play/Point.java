package com.jaws.ta4j.play;

/**
 *
 * @author tonyj
 */
public class Point {
    private final int index;
    private final double value;

    public Point(int index, double value) {
        this.index = index;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public double getValue() {
        return value;
    }
}
