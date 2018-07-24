/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jaws.ta4j.play;

import org.ta4j.core.Decimal;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.AbstractIndicator;

/**
 *
 * @author arujohn
 */
public class KernalIndicator extends AbstractIndicator{
    double sigma = 3.0;
    
    public double gaussian(int d){
        return Math.exp(-d*d/(2*sigma*sigma));
    }
    public double function(int i){
        return getTimeSeries().getBar(i).getClosePrice().doubleValue();
    }
    public KernalIndicator(TimeSeries series) {
        super(series);
    }
    public double regression(int i){
        double sum = 0;
        double sumW = 0;
        for(int n=getTimeSeries().getBeginIndex(); n<getTimeSeries().getEndIndex(); n++){
            final double gaussianValue = gaussian(n-i);
            sum+= gaussianValue*function(n);
            sumW+= gaussianValue;
        }
        return sum/sumW;
    }
    @Override
    public Object getValue(int index) {
        return Decimal.valueOf(regression(index));
    }
    
}
