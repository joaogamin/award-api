package com.outsera.award.service;

import com.outsera.award.dto.AwardIntervalResponse;
import com.outsera.award.dto.IntervalData;
import com.outsera.award.model.ProducerWin;
import com.outsera.award.repository.ProducerWinRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AwardService {

    private static final Logger log = LoggerFactory.getLogger(AwardService.class);

    private final ProducerWinRepository repository;

    @Autowired
    public AwardService(ProducerWinRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void initData() {
        repository.deleteAll();
        int count = 0;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        new ClassPathResource("movielist.csv").getInputStream()
                ))) {

            String line = br.readLine();

            while ((line = br.readLine()) != null) {
                String[] data = line.split(";");

                if (data.length < 4) continue;

                boolean isWinner = data.length == 5 &&
                        "yes".equalsIgnoreCase(data[4].trim());

                if (isWinner) {
                    Integer year = Integer.parseInt(data[0].trim());
                    String[] producers = parseProducers(data[3]);

                    for (String producer : producers) {
                        repository.save(new ProducerWin(producer, year));
                        count++;
                    }
                }
            }

            log.info("Loaded {} producer win records from movielist.csv", count);
        } catch (Exception e) {
            log.error("Failed to load initial data from movielist.csv", e);
        }
    }

    private String[] parseProducers(String rawProducers) {
        return Arrays.stream(rawProducers.replace(" and ", ", ").split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    public AwardIntervalResponse calculateIntervals() {
        log.info("Starting calculation of award intervals...");

        List<ProducerWin> allWins = repository.findAllByOrderByWinYearAsc();
        log.info("Fetched {} total win records from the database", allWins.size());

        Map<String, List<Integer>> winsByProducer = allWins.stream()
                .collect(Collectors.groupingBy(
                        ProducerWin::getProducerName,
                        Collectors.mapping(
                                ProducerWin::getWinYear,
                                Collectors.toList()
                        )
                ));
        log.info("Grouped wins by producer. Found {} unique producers.", winsByProducer.size());

        List<IntervalData> allIntervals = new ArrayList<>();

        winsByProducer.forEach((producer, years) -> {
            if (years.size() > 1) {
                log.debug("Calculating intervals for producer '{}' who has {} wins", producer, years.size());
                // groupingBy does not guarantee encounter order within each group
                Collections.sort(years);

                for (int i = 0; i < years.size() - 1; i++) {
                    int previousWin = years.get(i);
                    int followingWin = years.get(i + 1);
                    int interval = followingWin - previousWin;

                    log.debug("Interval for '{}': {} years ({} to {})", producer, interval, previousWin, followingWin);

                    allIntervals.add(
                            new IntervalData(producer, interval,
                                    previousWin, followingWin)
                    );
                }
            }
        });

        log.info("Finished calculating intervals. Generated {} discrete intervals from producers with multiple wins.", allIntervals.size());

        if (allIntervals.isEmpty()) {
            log.info("No producers with multiple wins were found. Returning empty lists.");
            return new AwardIntervalResponse(
                    Collections.emptyList(), Collections.emptyList());
        }

        int minInterval = allIntervals.stream()
                .mapToInt(IntervalData::getInterval)
                .min().getAsInt();

        int maxInterval = allIntervals.stream()
                .mapToInt(IntervalData::getInterval)
                .max().getAsInt();

        log.info("Determined the overall minimum interval to be {} years and the maximum to be {} years.", minInterval, maxInterval);

        Comparator<IntervalData> byPreviousWinThenProducer =
                Comparator.comparingInt(IntervalData::getPreviousWin)
                        .thenComparing(IntervalData::getProducer);

        List<IntervalData> minList = allIntervals.stream()
                .filter(i -> i.getInterval() == minInterval)
                .sorted(byPreviousWinThenProducer)
                .collect(Collectors.toList());
        log.info("Found {} interval record(s) matching the minimum interval of {} years.", minList.size(), minInterval);

        List<IntervalData> maxList = allIntervals.stream()
                .filter(i -> i.getInterval() == maxInterval)
                .sorted(byPreviousWinThenProducer)
                .collect(Collectors.toList());
        log.info("Found {} interval record(s) matching the maximum interval of {} years.", maxList.size(), maxInterval);

        log.info("Calculation completed successfully. Returning final response.");
        return new AwardIntervalResponse(minList, maxList);
    }
}