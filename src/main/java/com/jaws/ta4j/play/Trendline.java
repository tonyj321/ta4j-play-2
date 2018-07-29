/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jaws.ta4j.play;

import java.util.ArrayList;
import java.util.List;
import org.ta4j.core.Decimal;
import org.ta4j.core.Indicator;

/**
 *
 * @author arujohn
 */

public class Trendline {
    double slope = 0;
    int startIndex = 0; 
    double startPrice = 0;
    public Trendline(double slope, int startIndex, double startPrice){
        this.slope = slope;
        this.startIndex = startIndex;
        this.startPrice = startPrice;
    }
    public double getPrice(int index){
        return startPrice + (slope * (index - startIndex));
    }
    public int getStartIndex(){
        return startIndex;
    }
    public double getSlope(){
        return slope;
    }
}
