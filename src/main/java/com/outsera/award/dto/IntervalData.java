package com.outsera.award.dto;

public class IntervalData {
    private String producer;
    private Integer interval;
    private Integer previousWin;
    private Integer followingWin;

    public IntervalData(String producer, Integer interval,
                        Integer previousWin, Integer followingWin) {
        this.producer = producer;
        this.interval = interval;
        this.previousWin = previousWin;
        this.followingWin = followingWin;
    }

    public String getProducer() { return producer; }
    public Integer getInterval() { return interval; }
    public Integer getPreviousWin() { return previousWin; }
    public Integer getFollowingWin() { return followingWin; }
}