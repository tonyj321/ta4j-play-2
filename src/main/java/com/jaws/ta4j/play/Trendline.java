package com.jaws.ta4j.play;

/**
 *
 * @author arujohn
 */
public class Trendline {

    private final double slope;
    private final int startIndex;
    private final double startPrice;

    public Trendline(double slope, int startIndex, double startPrice) {
        this.slope = slope;
        this.startIndex = startIndex;
        this.startPrice = startPrice;
    }

    /**
     * Create a line joining two points
     * @param index1
     * @param doubleValue1
     * @param index2
     * @param doubleValue2 
     */
    Trendline(Integer index1, double doubleValue1, Integer index2, double doubleValue2) {
        this.slope = (doubleValue2 - doubleValue1) / (index2 - index1);
        this.startIndex = index1;
        this.startPrice = doubleValue1;
    }

    Trendline(Point p1, Point p2) {
        this.slope = (p2.getValue() - p1.getValue()) / (p2.getIndex() - p1.getIndex());
        this.startIndex = p1.getIndex();
        this.startPrice = p1.getValue();
    }

    public double getPrice(int index) {
        return startPrice + (slope * (index - startIndex));
    }

    public int getStartIndex() {
        return startIndex;
    }

    public double getSlope() {
        return slope;
    }

    double distanceFrom(Integer index, double doubleValue) {
        return doubleValue - getPrice(index);
    }

    double distanceFrom(Point p) {
        return p.getValue() - getPrice(p.getIndex());
    }
}
