package com.jaws.ta4j.play;

import java.util.ArrayList;
import java.util.List;
import org.ta4j.core.Decimal;
import org.ta4j.core.Indicator;

/**
 *
 * @author arujohn
 */
public class TrendlineIdentifier {

    private final List<Point> maximas = new ArrayList<>();
    private final List<Point> minimas = new ArrayList<>();
    private final List<Trendline> maximaTrends = new ArrayList<>();
    private final List<Trendline> minimaTrends = new ArrayList<>();

    public TrendlineIdentifier(Indicator<Decimal> minMaxIndicator, Indicator<Decimal> priceIndicator) {
        for (int ind = minMaxIndicator.getTimeSeries().getBeginIndex(); ind < minMaxIndicator.getTimeSeries().getBarCount(); ind++) {
            if (minMaxIndicator.getValue(ind).doubleValue() == 1) {
                maximas.add(new Point(ind, priceIndicator.getValue(ind).doubleValue()));
            } else if (minMaxIndicator.getValue(ind).doubleValue() == -1) {
                minimas.add(new Point(ind, priceIndicator.getValue(ind).doubleValue()));
            }
        }
        System.out.println(maximas.size());
        for (int max = 0; max < maximas.size(); max++) {
            Point p1 = maximas.get(max);
            for (int max2 = max+1; max2 < maximas.size(); max2++) {
                final Point p2 = maximas.get(max2);
                Trendline currentLine = new Trendline(p1, p2);
                double quality = 0;
                for (int max3 = max2+1; max3 < maximas.size(); max3++) {
                    final Point p3 = maximas.get(max3);
                    double distance = currentLine.distanceFrom(p3);
                    if (Math.abs(distance/p3.getValue())<0.01) {
                        quality++;
                    }
                }
                //double finalProjected = currentLine.getPrice(priceIndicator.getTimeSeries().getEndIndex());
                //double finalPrice = priceIndicator.getTimeSeries().getLastBar().getClosePrice().toDouble();
                if (quality > 4) {
                    maximaTrends.add(currentLine);
                }
            }
        }
    }

    public List<Trendline> getMaximaLines() {
        return maximaTrends;
    }

    public List<Trendline> getMinimaLines() {
        return minimaTrends;
    }
}
