package com.jaws.ta4j.play;

import java.awt.BasicStroke;
import java.awt.Color;
import java.sql.SQLException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.ta4j.core.Bar;
import org.ta4j.core.Decimal;
import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jfree.chart.plot.ValueMarker;

/**
 * This class builds a graphical chart showing values from indicators.
 */
public class IndicatorsToChart {

    /**
     * Builds a JFreeChart time series from a Ta4j time series and an indicator.
     * @param barseries the ta4j time series
     * @param indicator the indicator
     * @param name the name of the chart time series
     * @return the JFreeChart time series
     */
    private static org.jfree.data.time.TimeSeries buildChartTimeSeries(TimeSeries barseries, Indicator<Decimal> indicator, String name) {
        org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries(name);
        for (int i = 0; i < barseries.getBarCount(); i++) {
            Bar bar = barseries.getBar(i);
            chartTimeSeries.add(new Day(Date.from(bar.getEndTime().toInstant())), indicator.getValue(i).doubleValue());
        }
        return chartTimeSeries;
    }
    
    private static List<org.jfree.data.time.TimeSeries> buildTrendlines(TimeSeries barseries, List<Trendline> Trendlines, String name) {
        List<org.jfree.data.time.TimeSeries> trendlineSeries = new ArrayList<>();
        for (Trendline t:Trendlines){
            org.jfree.data.time.TimeSeries timSeries = new org.jfree.data.time.TimeSeries(name);
            Bar bar = barseries.getBar(t.getStartIndex());
            timSeries.add(new Day(Date.from(bar.getEndTime().toInstant())),t.getPrice(t.getStartIndex()));
            Bar lastBar = barseries.getBar(barseries.getEndIndex());
            timSeries.add(new Day(Date.from(lastBar.getEndTime().toInstant())),t.getPrice(barseries.getEndIndex()));
            trendlineSeries.add(timSeries);
        }
        return trendlineSeries;
    }

    /**
     * Displays a chart in a frame.
     * @param chart the chart to be displayed
     */
    private static void displayChart(JFreeChart chart) {
        // Chart panel
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        panel.setPreferredSize(new java.awt.Dimension(500, 270));
        // Application frame
        ApplicationFrame frame = new ApplicationFrame("Ta4j example - Indicators to chart");
        frame.setContentPane(panel);
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }

    public static void main(String[] args) throws SQLException {

        /*
          Getting time series
         */
        H2Loader loader = new H2Loader();
        TimeSeries series = loader.getTimeSeries("AAPL").getSubSeries(0, 2000);

        /*
          Creating indicators
         */
        // Close price
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        EMAIndicator avg7 = new EMAIndicator(closePrice, 7);
        EMAIndicator avg14 = new EMAIndicator(closePrice, 14);
        KernalIndicator kern50 = new KernalIndicator(series);
        MinMaxIndicator minMax = new MinMaxIndicator(kern50);
        StandardDeviationIndicator sd14 = new StandardDeviationIndicator(closePrice, 14);
        TrendlineIdentifier ti84 = new TrendlineIdentifier(minMax, kern50);

        // Bollinger bands
        BollingerBandsMiddleIndicator middleBBand = new BollingerBandsMiddleIndicator(avg14);
        BollingerBandsLowerIndicator lowBBand = new BollingerBandsLowerIndicator(middleBBand, sd14);
        BollingerBandsUpperIndicator upBBand = new BollingerBandsUpperIndicator(middleBBand, sd14);

        /*
          Building chart dataset
         */
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(buildChartTimeSeries(series, closePrice, "Apple Inc. (AAPL) - NASDAQ GS"));
        dataset.addSeries(buildChartTimeSeries(series, kern50, "Kernal"));
        //dataset.addSeries(buildChartTimeSeries(series, minMax, "minMax"));
        List<org.jfree.data.time.TimeSeries> trendlinesToAdd = buildTrendlines(series, ti84.getMaximaLines(), "Trendlines");
        System.out.println(ti84.getMaximaLines().size());
        System.out.println(trendlinesToAdd.size());
        for(org.jfree.data.time.TimeSeries ts:trendlinesToAdd){
            dataset.addSeries(ts);
        }
        

        /*
          Creating the chart
         */
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Apple Inc. Close Prices", // title
                "Date", // x-axis label
                "Price Per Unit", // y-axis label
                dataset, // data
                true, // create legend?
                true, // generate tooltips?
                false // generate URLs?
                );
        XYPlot plot = (XYPlot) chart.getPlot();
        /*
        for(int a=0; a<minMax.getTimeSeries().getBarCount(); a++){
            if (((Decimal) minMax.getValue(a)).doubleValue() < 0){
                Bar bar = minMax.getTimeSeries().getBar(a);
                Day day = new Day(Date.from(bar.getEndTime().toInstant()));
                ValueMarker mark = new ValueMarker(day.getFirstMillisecond(), Color.RED, new BasicStroke(1), Color.RED, null, 0.5f);
                plot.addDomainMarker(mark);
            }
            else if (((Decimal) minMax.getValue(a)).doubleValue() > 0){
                Bar bar = minMax.getTimeSeries().getBar(a);
                Day day = new Day(Date.from(bar.getEndTime().toInstant()));
                ValueMarker mark = new ValueMarker(day.getFirstMillisecond(), Color.BLUE, new BasicStroke(1), Color.BLUE, null, 0.5f);
                plot.addDomainMarker(mark);
            }
        }
        */
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));

        /*
          Displaying the chart
         */
        displayChart(chart);
    }

}