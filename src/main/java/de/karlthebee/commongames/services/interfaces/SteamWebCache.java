package de.karlthebee.commongames.services.interfaces;

import de.karlthebee.commongames.dto.steam.SteamFriendData;
import de.karlthebee.commongames.dto.steam.SteamGameData;
import de.karlthebee.commongames.dto.steam.SteamProfileData;

import java.util.concurrent.ExecutionException;

public interface SteamWebCache {

    String profileId(String id) throws ExecutionException;

    SteamProfileData profileData(String id) throws ExecutionException;

    SteamGameData gameData(String id) throws ExecutionException;

    SteamFriendData friendData(String id) throws ExecutionException;

}
