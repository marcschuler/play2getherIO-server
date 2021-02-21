package de.karlthebee.commongames.services;

import de.karlthebee.commongames.services.interfaces.StatisticService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

@Service
public class StatisticServiceImpl implements StatisticService {

    private static final Map<LocalDateTime, Integer> stats = new HashMap<>();


    public synchronized void makeAuthorizedRequest() {
        var time = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        if (!stats.containsKey(time)) {
            stats.put(time, 0);
        }
        stats.put(time, stats.get(time) + 1);
    }

    public long getLast15Minutes() {
        return getLastInterval(LocalDateTime.now().minusMinutes(15));
    }

    public long getLastHour() {
        return getLastInterval(LocalDateTime.now().minusHours(1));
    }

    public long getLast3Hours() {
        return getLastInterval(LocalDateTime.now().minusHours(3));
    }

    public long getLast24Hours() {
        return getLastInterval(LocalDateTime.now().minusHours(24));
    }

    public long getLastInterval(LocalDateTime datetime) {
        var now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        datetime = datetime.truncatedTo(ChronoUnit.MINUTES);
        long value = 0;
        while (!datetime.isAfter(now)) {
            if (stats.containsKey(datetime))
                value += stats.get(datetime);
            datetime = datetime.plusMinutes(1);
        }
        return value;
    }
}
