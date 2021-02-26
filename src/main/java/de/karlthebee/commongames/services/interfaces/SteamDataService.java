package de.karlthebee.commongames.services.interfaces;

import de.karlthebee.commongames.model.Profile;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface SteamDataService {
    Profile getProfile(String id) throws ExecutionException;

    /**
     *
     * @param id the profile id
     * @return a Profile object but without friends and games filled - is cheap and fast
     */
    Profile getEmptyFriendProfile(String id);

    String getGameName(String id) throws ExecutionException;

    Map<String, String> fetchGames();

    Profile fetchProfile(String id);
}
