package com.jaws.ta4j.play;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
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
    H2Loader() throws SQLException {
        this("jdbc:h2:~/Data/nasdaq;SCHEMA=PUBLIC;IFEXISTS=TRUE");
    }
    
    H2Loader(String url) throws SQLException {
        conn = DriverManager.getConnection(url);
    }
    
    Set<String> getAvailableSymbols() throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("select distinct symbol from bars order by symbol");
        ResultSet rs = stmt.executeQuery();
        Set<String> result = new TreeSet<>();
        while (rs.next()) {
            result.add(rs.getString(1));
        }
        return result;
    }
    
    TimeSeries getTimeSeries(String symbol) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("select date,open,close,high,low,volume from bars where symbol=? order by date");
        stmt.setString(1, symbol);
        ResultSet rs = stmt.executeQuery();
        List<Bar> bars = new ArrayList<>();
        while (rs.next()) {
            long date = rs.getLong(1);
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(date),ZoneId.of("America/New_York"));
            double open = rs.getDouble(2);
            double close = rs.getDouble(3);
            double high = rs.getDouble(4);
            double low = rs.getDouble(5);
            double volume = rs.getDouble(6);
            BaseBar bar = new BaseBar(zdt.plusDays(1), open, high, low, close, volume);
            bars.add(bar);
        }
        return new BaseTimeSeries(symbol,bars);        
    }

    @Override
    public void close() throws SQLException {
        conn.close();
    }
    
    public static void main(String[] args) throws SQLException {
        H2Loader h2 = new H2Loader();
        Set<String> availableSymbols = h2.getAvailableSymbols();
        System.out.println(availableSymbols);
        TimeSeries series = h2.getTimeSeries("AAPL");
        System.out.println(series.getBarCount());
        System.out.println(series.getFirstBar().getBeginTime());
        System.out.println(series.getFirstBar().getEndTime());
        System.out.println(series.getLastBar().getBeginTime());
        System.out.println(series.getLastBar().getEndTime());
    }
}
