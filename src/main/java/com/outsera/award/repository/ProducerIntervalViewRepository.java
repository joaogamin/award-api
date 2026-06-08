package com.outsera.award.repository;

import com.outsera.award.model.ProducerIntervalView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProducerIntervalViewRepository extends JpaRepository<ProducerIntervalView, Long> {

    @Query("SELECT MIN(v.intervalDiff) FROM ProducerIntervalView v WHERE v.intervalDiff IS NOT NULL")
    Integer findMinIntervalDiff();

    @Query("SELECT MAX(v.intervalDiff) FROM ProducerIntervalView v WHERE v.intervalDiff IS NOT NULL")
    Integer findMaxIntervalDiff();

    List<ProducerIntervalView> findByIntervalDiff(Integer intervalDiff);
}
