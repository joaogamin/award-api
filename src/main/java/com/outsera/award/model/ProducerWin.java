package com.outsera.award.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@Entity
public class ProducerWin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String producerName;
    private Integer winYear;

    @PrePersist
    @PreUpdate
    void normalizeFields() {
        if (producerName != null) producerName = producerName.trim();
    }

    public ProducerWin() {
    }

    public ProducerWin(String producerName, Integer winYear) {
        this.producerName = producerName;
        this.winYear = winYear;
    }

    public Long getId() { return id; }
    public String getProducerName() { return producerName; }
    public Integer getWinYear() { return winYear; }
}