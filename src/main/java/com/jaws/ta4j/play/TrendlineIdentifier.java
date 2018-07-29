/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jaws.ta4j.play;

import static java.nio.file.Files.list;
import static java.rmi.Naming.list;
import java.util.ArrayList;
import static java.util.Collections.list;
import java.util.List;
import org.ta4j.core.Decimal;
import org.ta4j.core.Indicator;

/**
 *
 * @author arujohn
 */
public class TrendlineIdentifier{
    List<Integer> maximas = new ArrayList<>();
    List<Integer> minimas = new ArrayList<>();
    List<Trendline> maximaTrends = new ArrayList<>();
    List<Trendline> minimaTrends = new ArrayList<>();
    public TrendlineIdentifier(Indicator<Decimal> minMaxIndicator, Indicator<Decimal> priceIndicator){
        for(int ind=minMaxIndicator.getTimeSeries().getBeginIndex(); ind<minMaxIndicator.getTimeSeries().getBarCount(); ind++){
            if (minMaxIndicator.getValue(ind).toDouble() == 1){
                maximas.add(ind);
            }
            else if (minMaxIndicator.getValue(ind).toDouble() == -1){
                minimas.add(ind);
            }
        }
        System.out.println(maximas.size());
        for(int max=0; max<maximas.size(); max++){
            for (int max2=max; max2<maximas.size(); max2++){
                double slope = (priceIndicator.getValue(max2).toDouble() - priceIndicator.getValue(max).toDouble())/(max2-max);
                Trendline currentLine = new Trendline(slope, maximas.get(max), priceIndicator.getValue(maximas.get(max)).toDouble());
                double quality = 0;
                for (int max3=max2; max3<maximas.size(); max3++){
                    if (currentLine.getPrice(max3) < priceIndicator.getValue(max3).toDouble()*1.02 && currentLine.getPrice(max3) > priceIndicator.getValue(max3).toDouble()*0.98){
                        quality+=1;
                    }
                }
                double finalProjected = currentLine.getPrice(priceIndicator.getTimeSeries().getEndIndex());
                double finalPrice = priceIndicator.getTimeSeries().getLastBar().getClosePrice().toDouble();
                if (quality > 5){
                    maximaTrends.add(currentLine);
                }
            }
        }
    }
    public List<Trendline> getMaximaLines(){
        return maximaTrends;
    }
    public List<Trendline> getMinimaLines(){
        return minimaTrends;
    }
}
