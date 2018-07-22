package com.jaws.ta4j.play;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;

/**
 *
 * @author tonyj
 */
public class H2Writer {

    public static void main(String[] args) throws IOException, SQLException {
        NASDAQDataReader reader = new NASDAQDataReader(Paths.get("/home/tonyj/Data/NASDAQ"));
        Map<String, TimeSeries> timeSeriesMap = reader.getTimeSeriesMap();
        try (Connection conn = DriverManager.getConnection("jdbc:h2:~/Data/nasdaq;SCHEMA=PUBLIC")) {
//            PreparedStatement create = conn.prepareStatement("CREATE TABLE BARS (SYMBOL VARCHAR(10), DATE LONG, OPEN DOUBLE, CLOSE DOUBLE, HIGH DOUBLE, LOW DOUBLE, VOLUME INT)");
//            create.execute();
            PreparedStatement deleteStmt = conn.prepareStatement("delete from BARS");
            deleteStmt.execute();
            PreparedStatement insertStmt = conn.prepareStatement("insert into BARS values (?,?,?,?,?,?,?)");
            for (Map.Entry<String, TimeSeries> entry : timeSeriesMap.entrySet()) {
                String symbol = entry.getKey();
                TimeSeries series = entry.getValue();
                for (Bar bar : series.getBarData()) {
                    insertStmt.setString(1, symbol);
                    insertStmt.setLong(2, bar.getBeginTime().toEpochSecond());
                    insertStmt.setDouble(3,bar.getOpenPrice().doubleValue());
                    insertStmt.setDouble(4,bar.getClosePrice().doubleValue());
                    insertStmt.setDouble(5,bar.getMaxPrice().doubleValue());
                    insertStmt.setDouble(6,bar.getMinPrice().doubleValue());
                    insertStmt.setDouble(7,bar.getVolume().doubleValue());
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }
        }
    }
}
