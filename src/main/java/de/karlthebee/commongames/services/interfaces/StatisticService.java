package de.karlthebee.commongames.services.interfaces;

import java.time.LocalDateTime;

public interface StatisticService {

    void makeAuthorizedRequest();

    long getLast15Minutes();

    long getLastHour();

    long getLast3Hours();

    long getLast24Hours();

    long getLastInterval(LocalDateTime datetime);
}
