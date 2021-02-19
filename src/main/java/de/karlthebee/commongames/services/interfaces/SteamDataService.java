package de.karlthebee.commongames.services.interfaces;

import de.karlthebee.commongames.model.Profile;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface SteamDataService {
    Profile getProfile(String id) throws ExecutionException;

    String getGameName(String id) throws ExecutionException;

    String fetchProfileId(String id);

    Map<String, String> fetchGames();

    Profile fetchProfile(String id);
}
