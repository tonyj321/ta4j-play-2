package com.jaws.ta4j.play;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;

/**
 *
 * @author tonyj
 */
public class H2Loader implements AutoCloseable {

    private final Connection conn;
    private final PreparedStatement timeSeriesStatement;
    private final PreparedStatement timeSeriesStatement2;
    private final PreparedStatement availableStatement;

    H2Loader() throws SQLException {
        this("jdbc:h2:~/Data/nasdaq;SCHEMA=PUBLIC;IFEXISTS=TRUE");
    }

    H2Loader(String url) throws SQLException {
        conn = DriverManager.getConnection(url);
        timeSeriesStatement = conn.prepareStatement("select date,open,close,high,low,volume from bars where symbol=? order by date");
        timeSeriesStatement2 = conn.prepareStatement("select date,open,close,high,low,volume from bars where symbol=? and date>=? and date <? order by date");
        availableStatement = conn.prepareStatement("select distinct symbol from bars order by symbol");
    }

    Set<String> getAvailableSymbols() throws SQLException {
        try (ResultSet rs = availableStatement.executeQuery()) {
            Set<String> result = new TreeSet<>();
            while (rs.next()) {
                result.add(rs.getString(1));
            }
            return result;
        }
    }

    TimeSeries getTimeSeries(String symbol) throws SQLException {
        timeSeriesStatement.setString(1, symbol);
        try (ResultSet rs = timeSeriesStatement.executeQuery()) {
            return resulltSetToTimeSerier(symbol, rs);
        }
    }

    TimeSeries getTimeSeries(String symbol, ZonedDateTime start, ZonedDateTime end) throws SQLException {
        long t1 = start.toEpochSecond();
        long t2 = end.toEpochSecond();
        timeSeriesStatement2.setString(1, symbol);
        timeSeriesStatement2.setLong(2, t1);
        timeSeriesStatement2.setLong(3, t2);
        try (ResultSet rs = timeSeriesStatement2.executeQuery()) {
           return resulltSetToTimeSerier(symbol,rs);
        }
    }

    private TimeSeries resulltSetToTimeSerier(String symbol, ResultSet rs) throws SQLException {
        List<Bar> bars = new ArrayList<>();
        while (rs.next()) {
            long date = rs.getLong(1);
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(date), ZoneId.of("America/New_York"));
            double open = rs.getDouble(2);
            double close = rs.getDouble(3);
            double high = rs.getDouble(4);
            double low = rs.getDouble(5);
            double volume = rs.getDouble(6);
            BaseBar bar = new BaseBar(zdt.plusDays(1), open, high, low, close, volume);
            bars.add(bar);
        }
        return new BaseTimeSeries(symbol, bars);
    }

    @Override
    public void close() throws SQLException {
        conn.close();
    }

    public static void main(String[] args) throws SQLException {
        H2Loader h2 = new H2Loader();
        Set<String> availableSymbols = h2.getAvailableSymbols();
        System.out.println(availableSymbols);
        ZonedDateTime start = ZonedDateTime.of(LocalDate.of(2010, Month.JANUARY, 1), LocalTime.MIDNIGHT, ZoneId.of("America/New_York"));
        ZonedDateTime end = ZonedDateTime.of(LocalDate.of(2010, Month.DECEMBER, 31), LocalTime.MIDNIGHT, ZoneId.of("America/New_York"));
        TimeSeries series = h2.getTimeSeries("AAPL", start, end);
        System.out.println(series.getBarCount());
        System.out.println(series.getFirstBar().getBeginTime());
        System.out.println(series.getFirstBar().getEndTime());
        System.out.println(series.getLastBar().getBeginTime());
        System.out.println(series.getLastBar().getEndTime());
    }
}
