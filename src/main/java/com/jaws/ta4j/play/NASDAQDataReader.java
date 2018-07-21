package com.jaws.ta4j.play;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;

/**
 *
 * @author tonyj
 */
public final class NASDAQDataReader {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private Map<String, TimeSeries> timeSeriesMap;
    
    NASDAQDataReader(Path dir) throws IOException {
        try {
            timeSeriesMap = scanDirectory(dir);
        } catch (FileNotFoundException | ParseException ex) {
            throw new IOException("Error reading NASDAQ data", ex);
        } 
    }

    TimeSeries getTimeSeries(String symbol) {
        return timeSeriesMap.get(symbol);
    }

    Map<String, TimeSeries> scanDirectory(Path directory) throws IOException, FileNotFoundException, ParseException {
        Map<String, List<Bar>> fullBarMap = new HashMap<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(directory)) {
            for (Path child : ds) {
                Map<String, List<Bar>> barMap = readZIPFile(child);
                merge(fullBarMap, barMap);
            }
        }
        Map<String, TimeSeries> result = new HashMap<>();
        fullBarMap.forEach((symbol, bars) -> {
            Collections.sort(bars, (Bar b1, Bar b2) -> b1.getBeginTime().compareTo(b2.getBeginTime()));
            TimeSeries ts = new BaseTimeSeries(symbol,bars);
            result.put(symbol,ts);
        });
        return result;
    }

    Map<String, List<Bar>> readZIPFile(Path fileName) throws FileNotFoundException, IOException, ParseException {
        Map<String, List<Bar>> barMap = new HashMap<>();
        try (FileInputStream fileIn = new FileInputStream(fileName.toFile());
                ZipInputStream zip = new ZipInputStream(fileIn);
                InputStreamReader in = new InputStreamReader(zip)) {
            for (;;) {
                ZipEntry zipEntry = zip.getNextEntry();
                if (zipEntry == null) {
                    break;
                }
                BufferedReader reader = new BufferedReader(in);
                for (;;) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    String[] data = line.split(",");
                    String symbol = data[0];
                    ZonedDateTime date = LocalDate.parse(data[1], DATE_FORMAT).atStartOfDay(ZoneId.systemDefault());
                    double open = Double.parseDouble(data[2]);
                    double high = Double.parseDouble(data[3]);
                    double low = Double.parseDouble(data[4]);
                    double close = Double.parseDouble(data[5]);
                    double volume = Double.parseDouble(data[6]);
                    BaseBar bar = new BaseBar(date, open, high, low, close, volume);
                    List<Bar> bars = barMap.get(symbol);
                    if (bars == null) {
                        bars = new ArrayList<>();
                        barMap.put(symbol, bars);
                    }
                    bars.add(bar);
                }
            }
        }
        return barMap;
    }

    private void merge(Map<String, List<Bar>> fullBarMap, Map<String, List<Bar>> barMap) {
        barMap.forEach((symbol, bars) -> {
            List<Bar> existing = fullBarMap.get(symbol);
            if (existing == null) {
                fullBarMap.put(symbol, bars);
            } else {
                existing.addAll(bars);
            }
        });
    }
}
