package de.karlthebee.commongames.services;

import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.karlthebee.commongames.dto.steam.*;
import de.karlthebee.commongames.model.Profile;
import de.karlthebee.commongames.services.interfaces.SteamDataService;
import de.karlthebee.commongames.services.interfaces.SteamIdService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SteamDataServiceImpl implements SteamDataService {

    private SteamIdService steamIdService;

    @Value("${steam.key}")
    private String key;
    @Value("${steam.profiles.cachetime}")
    private int steamProfilesCachetime;
    @Value("${steam.profileid.cachetime}")
    private int steamProfileidCachetime;


    private final LoadingCache<String, Profile> profiles =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(steamProfilesCachetime, TimeUnit.MINUTES)
                    .build(new CacheLoader<>() {
                        @Override
                        public Profile load(String id) {
                            return fetchProfile(id);
                        }
                    });

    private final LoadingCache<String, String> profileIds =
            CacheBuilder.newBuilder()
                    .expireAfterAccess(steamProfileidCachetime, TimeUnit.MINUTES)
                    .build(new CacheLoader<>() {
                        @Override
                        public String load(String id) {
                            return fetchProfileId(id);
                        }
                    });

    private final Supplier<Map<String, String>> games = Suppliers.memoizeWithExpiration(() -> fetchGames(), 30, TimeUnit.MINUTES);

    public SteamDataServiceImpl(SteamIdService steamIdService) {
        this.steamIdService = steamIdService;
    }


    @Override
    public Profile getProfile(String id) throws ExecutionException {
        var oldId = id;
        id = steamIdService.prepareId(id);

        //Find real 64b id
        if (!steamIdService.isId64(id)) {
            id = this.profileIds.get(oldId);
            log.info("Profile change '" + oldId + "' -> '" + id + "'");
        }

        return profiles.get(id);
    }


    @Override
    public String getGameName(String id) throws ExecutionException {
        return this.games.get().get(id);
    }

    @Override
    public String fetchProfileId(String id) {
        var newId = id.replace("https://steamcommunity.com/id/", "")
                .replace("http://steamcommunity.com/id/", "")
                .replace("/", "");


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

    @Override
    public Map<String, String> fetchGames() {
        log.info("(Re)loading all games");
        long start = System.currentTimeMillis();
        var games = WebClient.builder()
                .baseUrl("https://api.steampowered.com/ISteamApps/GetAppList/v2")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) //Is 1.6MB long
                .build()
                .get()
                .retrieve()
                .toEntity(SteamAppData.class)
                .block();

        var map = new HashMap<String, String>();

        games.getBody().getApplist().getApps().stream()
                .forEach(app -> map.put(app.getAppid(), app.getName()));
        log.info("Got " + map.size() + " games in " + (System.currentTimeMillis() - start) + "ms");
        return map;
    }

    @Override
    public Profile fetchProfile(String id) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(key);

        var profileResponse = WebClient.create("http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002")
                .get()
                .uri(builder -> builder.queryParam("key", key).queryParam("steamids", id).build())
                .retrieve();
        var gameResponse = WebClient.create("api.steampowered.com/IPlayerService/GetOwnedGames/v0001")
                .get()
                .uri(builder -> builder.queryParam("key", key).queryParam("steamid", id).queryParam("include_played_free_games", true).build())
                .retrieve();
        var friendsResponse = WebClient.create("http://api.steampowered.com/ISteamUser/GetFriendList/v0001")
                .get()
                .uri(builder -> builder.queryParam("key", key).queryParam("steamid", id).queryParam("relationship", "friend").build())
                .retrieve();

        ResponseEntity<SteamFriendData> friendsData = null;
        ResponseEntity<SteamGameData> gameData;
        ResponseEntity<SteamProfileData> profileData;
        try {
            profileData = profileResponse.toEntity(SteamProfileData.class).block();
            gameData = gameResponse.toEntity(SteamGameData.class).block();
        } catch (WebClientResponseException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not get server data");
        }
        try {
            friendsData = friendsResponse.toEntity(SteamFriendData.class).block();
        } catch (WebClientResponseException e) {
            log.info("Could not get friendlist of " + id);
        }
        var player = profileData.getBody().getResponse().getPlayers()[0];
        if (gameData.getBody().getResponse().getGames() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This user (" + id + ") does not exist or has no games");
        var games = gameData.getBody().getResponse().getGames().stream()
                .map(SteamGameData.SteamGameDataListItem::getAppid)
                .map(String::valueOf)
                .collect(Collectors.toList());
        var friends = friendsData == null ? new ArrayList<String>() : friendsData.getBody().getFriendslist().getFriends().stream()
                .map(SteamFriendData.SteamFriendDataItem::getSteamid)
                .collect(Collectors.toList());

        var profile = new Profile(player.getSteamid(), player.getPersonaname(), player.getAvatarfull(), player.getProfileurl(), games, friends);
        log.info("Profile " + id + " is " + profile.getNickname() + "(" + profile.getId() + ")");
        return profile;
    }
}
