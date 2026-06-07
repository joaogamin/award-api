package com.outsera.award.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "movie",
    indexes = {
        @Index(name = "idx_movie_release_year", columnList = "release_year"),
        @Index(name = "idx_movie_winner",        columnList = "winner")
    }
)
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "release_year", nullable = false)
    private Integer releaseYear;

    @Column(nullable = false)
    private Boolean winner;

    @ManyToMany
    @JoinTable(
        name = "movie_producer",
        joinColumns        = @JoinColumn(name = "movie_id"),
        inverseJoinColumns = @JoinColumn(name = "producer_id")
    )
    private List<Producer> producers = new ArrayList<>();

    public Movie() {}

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }

    public Boolean isWinner() { return winner; }
    public void setWinner(Boolean winner) { this.winner = winner; }

    public List<Producer> getProducers() { return producers; }
    public void setProducers(List<Producer> producers) { this.producers = producers; }
}
