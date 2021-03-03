package de.karlthebee.commongames.rest;

import de.karlthebee.commongames.dto.TimeStat;
import de.karlthebee.commongames.services.interfaces.StatisticService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("v1/statistics")
@CrossOrigin
@Data
@Slf4j
public class StatisticRest {

    private final StatisticService statisticService;

    @Value("${steam.api.maxDay}")
    private long apiMaxDay;
    private final List<Integer> timespan = List.of(5, 15, 60, 3 * 60, 6 * 60, 12 * 60, 24 * 60, 7 * 24 * 60);

    @GetMapping
    public List<TimeStat> get() {
        var timeEnd = LocalDateTime.now();
        return timespan.stream()
                .mapToInt(i -> i)
                .mapToObj(i -> {
                    var timeStart = timeEnd.minusMinutes(i);
                    var maxValue = apiMaxDay * i / 24 / 60;
                    var value = statisticService.getLastInterval(timeStart);
                    return TimeStat.builder()
                            .timeEnd(timeEnd)
                            .timeStart(timeStart)
                            .maxValue(maxValue)
                            .value(value)
                            .minutes(i)
                            .percentage(((double) value) / maxValue)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
