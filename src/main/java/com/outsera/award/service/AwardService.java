package com.outsera.award.service;

import com.outsera.award.dto.AwardIntervalResponse;
import com.outsera.award.dto.IntervalData;
import com.outsera.award.model.Movie;
import com.outsera.award.model.Producer;
import com.outsera.award.model.ProducerIntervalView;
import com.outsera.award.repository.MovieRepository;
import com.outsera.award.repository.ProducerIntervalViewRepository;
import com.outsera.award.repository.ProducerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AwardService {

    private static final Logger log = LoggerFactory.getLogger(AwardService.class);
    private static final int BATCH_SIZE = 50;

    private final MovieRepository movieRepository;
    private final ProducerRepository producerRepository;
    private final ProducerIntervalViewRepository intervalViewRepository;
    private final CsvProcessorService csvProcessor;

    public AwardService(MovieRepository movieRepository,
                        ProducerRepository producerRepository,
                        ProducerIntervalViewRepository intervalViewRepository,
                        CsvProcessorService csvProcessor) {
        this.movieRepository = movieRepository;
        this.producerRepository = producerRepository;
        this.intervalViewRepository = intervalViewRepository;
        this.csvProcessor = csvProcessor;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initData() {
        if (movieRepository.count() > 0) {
            log.info("Database already populated — skipping CSV load (idempotency guard)");
            return;
        }

        List<CsvProcessorService.MovieRecord> records = csvProcessor.parseCsv("movielist.csv");

        Set<String> allNames = new LinkedHashSet<>();
        for (CsvProcessorService.MovieRecord r : records) {
            allNames.addAll(r.getProducers());
        }

        List<Producer> producerList = new ArrayList<>();
        for (String name : allNames) {
            producerList.add(new Producer(name));
        }
        for (int i = 0; i < producerList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, producerList.size());
            producerRepository.saveAll(producerList.subList(i, end));
        }
        log.info("Persisted {} unique producers", producerList.size());

        // Re-fetch producers so all entities are persistent (have IDs) in the current JVM heap
        Map<String, Producer> producerMap = producerRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Producer::getName, p -> p));

        List<Movie> movies = new ArrayList<>();
        for (CsvProcessorService.MovieRecord rec : records) {
            Movie movie = new Movie();
            movie.setTitle(rec.getTitle());
            movie.setReleaseYear(rec.getYear());
            movie.setWinner(rec.isWinner());

            List<Producer> movieProducers = new ArrayList<>();
            for (String name : rec.getProducers()) {
                Producer p = producerMap.get(name);
                if (p != null) movieProducers.add(p);
            }
            movie.setProducers(movieProducers);
            movies.add(movie);
        }

        for (int i = 0; i < movies.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, movies.size());
            movieRepository.saveAll(movies.subList(i, end));
        }
        log.info("Persisted {} movies from CSV", movies.size());
    }

    public AwardIntervalResponse calculateIntervals() {
        Integer minInterval = intervalViewRepository.findMinIntervalDiff();
        Integer maxInterval = intervalViewRepository.findMaxIntervalDiff();

        if (minInterval == null) {
            return new AwardIntervalResponse(Collections.emptyList(), Collections.emptyList());
        }

        Comparator<ProducerIntervalView> byYearThenName =
                Comparator.comparingInt(ProducerIntervalView::getPreviousWin)
                        .thenComparing(ProducerIntervalView::getProducerName);

        List<IntervalData> minList = intervalViewRepository.findByIntervalDiff(minInterval)
                .stream()
                .sorted(byYearThenName)
                .map(v -> new IntervalData(v.getProducerName(), v.getIntervalDiff(),
                        v.getPreviousWin(), v.getFollowingWin()))
                .collect(Collectors.toList());

        List<IntervalData> maxList = intervalViewRepository.findByIntervalDiff(maxInterval)
                .stream()
                .sorted(byYearThenName)
                .map(v -> new IntervalData(v.getProducerName(), v.getIntervalDiff(),
                        v.getPreviousWin(), v.getFollowingWin()))
                .collect(Collectors.toList());

        return new AwardIntervalResponse(minList, maxList);
    }
}
