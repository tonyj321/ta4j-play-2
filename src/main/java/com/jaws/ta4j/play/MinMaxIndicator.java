/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jaws.ta4j.play;

import org.ta4j.core.Decimal;
import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.AbstractIndicator;

/**
 *
 * @author arujohn
 */
public class MinMaxIndicator extends AbstractIndicator{
    double[] toReturn = new double[getTimeSeries().getBarCount()];
    public MinMaxIndicator(Indicator<Decimal> indicator) {
        super(indicator.getTimeSeries());
        for(int ind=getTimeSeries().getBeginIndex(); ind<getTimeSeries().getEndIndex(); ind++){
            if (ind == getTimeSeries().getBeginIndex()){
                toReturn[ind] = 0;
            }
            else if (ind == getTimeSeries().getEndIndex()){
                toReturn[ind] = 0;
            }
            else{
                if (indicator.getValue(ind).compareTo(indicator.getValue(ind-1)) == -1 && indicator.getValue(ind).compareTo(indicator.getValue(ind+1)) == -1){
                    toReturn[ind] = -1;
                }
                else if (indicator.getValue(ind).compareTo(indicator.getValue(ind-1)) == 1 && indicator.getValue(ind).compareTo(indicator.getValue(ind+1)) == 1){
                    toReturn[ind] = 1;
                }
                else{
                    toReturn[ind] = 0;
                }
            }
        }
    }
    @Override
    public Object getValue(int index) {
        return Decimal.valueOf(toReturn[index]); 
    }
}
