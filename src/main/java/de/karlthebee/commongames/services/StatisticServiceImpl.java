package de.karlthebee.commongames.services;

import de.karlthebee.commongames.services.interfaces.StatisticService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

@Service
public class StatisticServiceImpl implements StatisticService {

    private static final List<LocalDateTime> stats = new ArrayList<>();


    public void makeAuthorizedRequest() {
        stats.add(LocalDateTime.now());
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
        return stats.stream().filter(dt -> dt.isAfter(datetime))
                .count();
    }
}
