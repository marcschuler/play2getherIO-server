package de.karlthebee.commongames.dto;

import lombok.Builder;
import lombok.Data;
import org.apache.tomcat.jni.Local;

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
