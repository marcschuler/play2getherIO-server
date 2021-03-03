package de.karlthebee.commongames.services;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.karlthebee.commongames.dto.steam.SteamFriendData;
import de.karlthebee.commongames.dto.steam.SteamGameData;
import de.karlthebee.commongames.dto.steam.SteamIdData;
import de.karlthebee.commongames.dto.steam.SteamProfileData;
import de.karlthebee.commongames.services.interfaces.StatisticService;
import de.karlthebee.commongames.services.interfaces.SteamWebCache;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("NullableProblems")
@Service
@Slf4j
@RequiredArgsConstructor
public class SteamWebCacheImpl implements SteamWebCache {

    //ID QUERY
    @Getter
    private final LoadingCache<String, String> profileIds =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(6 * 60, TimeUnit.MINUTES)
                    .build(new CacheLoader<>() {
                        @Override
                        public String load(String id) {
                            return getProfileId(id);
                        }
                    });

    //BASIC PROFILE INFO
    @Getter
    private final LoadingCache<String, SteamProfileData> webProfileData =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(2 * 60, TimeUnit.MINUTES)
                    .build(new CacheLoader<>() {
                        @Override
                        public SteamProfileData load(String id) {
                            return getProfileData(id);
                        }
                    });

    //GAME LIST
    @Getter
    private final LoadingCache<String, SteamGameData> webGameData =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(15, TimeUnit.MINUTES)
                    .build(new CacheLoader<>() {
                        @Override
                        public SteamGameData load(String id) {
                            return getGameData(id);
                        }
                    });

    //FRIEND LIST
    @Getter
    private final LoadingCache<String, SteamFriendData> webFriendData =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(2 * 60, TimeUnit.MINUTES)
                    .build(new CacheLoader<>() {
                        @Override
                        public SteamFriendData load(String id) {
                            return getFriendData(id);
                        }
                    });


    @Value("${steam.key}")
    private String key;

    private final StatisticService statisticService;

    public String profileId(String id) throws ExecutionException {
        return profileIds.get(id);
    }

    public SteamProfileData profileData(String id) throws ExecutionException {
        return webProfileData.get(id);
    }

    public SteamGameData gameData(String id) throws ExecutionException {
        return webGameData.get(id);
    }

    public SteamFriendData friendData(String id) throws ExecutionException {
        return webFriendData.get(id);
    }

    private SteamProfileData getProfileData(String id) {
        statisticService.makeAuthorizedRequest();
        var profileResponse = WebClient.create("http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002")
                .get()
                .uri(builder -> builder.queryParam("key", key).queryParam("steamids", id).build())
                .retrieve();

        try {
            var profileData = profileResponse.toEntity(SteamProfileData.class).block();
            return profileData.getBody();
        } catch (WebClientResponseException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not get server data");
        }
    }

    private SteamGameData getGameData(String id) {
        var gameResponse = WebClient.create("api.steampowered.com/IPlayerService/GetOwnedGames/v0001")
                .get()
                .uri(builder -> builder.queryParam("key", key).queryParam("steamid", id).queryParam("include_played_free_games", true).build())
                .retrieve();
        try {
            var gameData = gameResponse.toEntity(SteamGameData.class).block();
            return gameData.getBody();
        } catch (WebClientResponseException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not get server data");
        }
    }

    private SteamFriendData getFriendData(String id) {
        statisticService.makeAuthorizedRequest();
        var friendsResponse = WebClient.create("http://api.steampowered.com/ISteamUser/GetFriendList/v0001")
                .get()
                .uri(builder -> builder.queryParam("key", key).queryParam("steamid", id).queryParam("relationship", "friend").build())
                .retrieve();
        try {
            var friendsData = friendsResponse.toEntity(SteamFriendData.class).block();
            return friendsData.getBody();
        } catch (WebClientResponseException e) {
            log.info("Could not get friendlist of " + id);
            return null;
        }
    }

    private String getProfileId(String id) {
        var newId = id.replace("https://steamcommunity.com/id/", "")
                .replace("http://steamcommunity.com/id/", "")
                .replace("/", "");

        statisticService.makeAuthorizedRequest();
        var profileIdData = WebClient.create("http://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/")
                .get()
                .uri(builder -> builder.queryParam("key", key).queryParam("vanityurl", newId).build())
                .retrieve()
                .toEntity(SteamIdData.class).block();

        var response = profileIdData.getBody().getResponse();
        if (response.getSuccess() != 1)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find user '" + newId + "' ('" + id + "')");
        return response.getSteamid();
    }

}
