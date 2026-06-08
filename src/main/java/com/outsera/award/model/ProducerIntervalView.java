package com.outsera.award.model;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Synchronize;

import javax.persistence.*;

@Entity
@Immutable
@Table(name = "vw_producer_intervals")
@Synchronize({"movie", "producer", "movie_producer"})
public class ProducerIntervalView {

    @Id
    private Long id;

    @Column(name = "producer_name")
    private String producerName;

    @Column(name = "previous_win")
    private Integer previousWin;

    @Column(name = "following_win")
    private Integer followingWin;

    @Column(name = "interval_diff")
    private Integer intervalDiff;

    public Long getId() { return id; }

    public String getProducerName() { return producerName; }

    public Integer getPreviousWin() { return previousWin; }

    public Integer getFollowingWin() { return followingWin; }

    public Integer getIntervalDiff() { return intervalDiff; }
}
