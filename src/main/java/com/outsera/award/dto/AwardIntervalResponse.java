package com.outsera.award.dto;

import java.util.List;

public class AwardIntervalResponse {
    private List<IntervalData> min;
    private List<IntervalData> max;

    public AwardIntervalResponse(List<IntervalData> min,
                                 List<IntervalData> max) {
        this.min = min;
        this.max = max;
    }

    public List<IntervalData> getMin() { return min; }
    public List<IntervalData> getMax() { return max; }
}