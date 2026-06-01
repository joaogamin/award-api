package com.outsera.award.repository;

import com.outsera.award.model.ProducerWin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProducerWinRepository
        extends JpaRepository<ProducerWin, Long> {

    List<ProducerWin> findAllByOrderByWinYearAsc();
}