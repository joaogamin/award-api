package com.outsera.award.model;

import javax.persistence.*;

@Entity
@Table(
    name = "producer",
    indexes = @Index(name = "idx_producer_name", columnList = "name")
)
public class Producer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    public Producer() {}

    public Producer(String name) {
        this.name = name;
    }

    public Long getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }
}
