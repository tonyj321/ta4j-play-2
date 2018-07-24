/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jaws.ta4j.play;

import java.util.Random;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author arujohn
 */
public class KernalRegression {
    double sigma = 3.0;
    double[] y_normal = new double[100];
    double[] y = new double[100];
    public KernalRegression(){
        Random r = new Random();
        for(int i = 0; i<100; i++){
            y_normal[i] = Math.sin(i/10.0) + Math.pow(i/50.0, 2);
            y[i] = Math.sin(i/10.0) + Math.pow(i/50.0, 2) + .2 * r.nextGaussian();
        }
    }
    public double function(int i){
        return y[i];
    }
    public double functionNormal(int i){
        return y_normal[i];
    }
    public double gaussian(int d){
        return Math.exp(-d*d/(2*sigma*sigma));
    }
    public static void main(String[] args){
        KernalRegression kr = new KernalRegression();
        XYSeries data = new XYSeries("data");
        XYSeries dataNormal = new XYSeries("dataNormal");
        XYSeries GaussRegression = new XYSeries("GaussRegression");
        for(int i = 0; i<100; i++){
            data.add(i,kr.function(i));
            dataNormal.add(i,kr.functionNormal(i));
            GaussRegression.add(i,kr.regression(i));
            System.out.println(kr.function(i));
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(data);
        dataset.addSeries(dataNormal);
        dataset.addSeries(GaussRegression);
        JFreeChart chart = ChartFactory.createScatterPlot("test", "value", "time", dataset);
        ChartPanel chartPanel = new ChartPanel(chart);
        JFrame frame = new JFrame("testPanel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }
    public double regression(int i){
        double sum = 0;
        double sumW = 0;
        for(int n=0; n<100; n++){
            final double gaussianValue = gaussian(n-i);
            sum+= gaussianValue*function(n);
            sumW+= gaussianValue;
        }
        return sum/sumW;
    }    
}
