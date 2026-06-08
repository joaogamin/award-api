package com.outsera.award.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CsvProcessorService {

    private static final Logger log = LoggerFactory.getLogger(CsvProcessorService.class);

    // Handles: ", and ", " and ", plain "," separators in producer names
    private static final Pattern PRODUCER_SPLITTER =
            Pattern.compile(",\\s+and\\s+|\\s+and\\s+|,\\s*");

    public List<MovieRecord> parseCsv(String classpathResource) {
        List<MovieRecord> records = new ArrayList<>();

        InputStream is = getClass().getClassLoader().getResourceAsStream(classpathResource);
        if (is == null) {
            log.error("CSV resource not found on classpath: {}", classpathResource);
            return Collections.emptyList();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (firstLine) { firstLine = false; continue; } // skip header
                if (line.trim().isEmpty()) continue;

                String[] cols = line.split(";");
                if (cols.length < 4) {
                    log.warn("Line {}: skipping — fewer than 4 columns: [{}]", lineNumber, line);
                    continue;
                }

                int year;
                try {
                    year = Integer.parseInt(cols[0].trim());
                } catch (NumberFormatException e) {
                    log.warn("Line {}: skipping — invalid year value: [{}]", lineNumber, cols[0].trim());
                    continue;
                }

                String title = cols[1].trim();
                String producersRaw = cols[3].trim();
                boolean winner = cols.length >= 5 && "yes".equalsIgnoreCase(cols[4].trim());

                List<String> producers = Arrays.stream(PRODUCER_SPLITTER.split(producersRaw))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());

                if (producers.isEmpty()) {
                    log.warn("Line {}: skipping — no producers parsed from: [{}]", lineNumber, producersRaw);
                    continue;
                }

                records.add(new MovieRecord(year, title, producers, winner));
            }

            log.info("Parsed {} movie records from {}", records.size(), classpathResource);
        } catch (Exception e) {
            log.error("Error reading CSV resource: {}", classpathResource, e);
        }

        return records;
    }

    public static class MovieRecord {
        private final int year;
        private final String title;
        private final List<String> producers;
        private final boolean winner;

        public MovieRecord(int year, String title, List<String> producers, boolean winner) {
            this.year = year;
            this.title = title;
            this.producers = producers;
            this.winner = winner;
        }

        public int getYear() { return year; }
        public String getTitle() { return title; }
        public List<String> getProducers() { return producers; }
        public boolean isWinner() { return winner; }
    }
}
