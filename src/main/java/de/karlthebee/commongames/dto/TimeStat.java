package de.karlthebee.commongames.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TimeStat {
    private LocalDateTime timeStart;
    private LocalDateTime timeEnd;
    private long minutes;
    private long value;
    private long maxValue;
    private double percentage;
}
