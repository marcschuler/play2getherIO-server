package de.karlthebee.commongames.services;

import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.karlthebee.commongames.dto.steam.*;
import de.karlthebee.commongames.model.Profile;
import de.karlthebee.commongames.services.interfaces.StatisticService;
import de.karlthebee.commongames.services.interfaces.SteamDataService;
import de.karlthebee.commongames.services.interfaces.SteamIdService;
import de.karlthebee.commongames.services.interfaces.SteamWebCache;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SteamDataServiceImpl implements SteamDataService {

    private final SteamIdService steamIdService;
    private final StatisticService statisticService;
    private final SteamWebCache steamWebCache;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(8);

    private final Supplier<Map<String, String>> games = Suppliers.memoizeWithExpiration(() -> fetchGames(), 30, TimeUnit.MINUTES);

    public SteamDataServiceImpl(SteamIdService steamIdService, StatisticService statisticService, SteamWebCache steamWebCache) {
        this.steamIdService = steamIdService;
        this.statisticService = statisticService;
        this.steamWebCache = steamWebCache;
    }


    @Override
    public Profile getProfile(String id) throws ExecutionException {
        var oldId = id;
        id = steamIdService.prepareId(id);

        //Find real 64b id
        if (!steamIdService.isId64(id)) {
            id = this.steamWebCache.profileId(id);
            log.info("Profile change '" + oldId + "' -> '" + id + "'");
        }

        return fetchProfile(id);
    }

    @Override
    public Profile getEmptyFriendProfile(String id) {
        var profileFuture = threadPool.submit(() -> steamWebCache.profileData(id));
        SteamProfileData profile = null;
        try {
            profile = profileFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        var player = profile.getResponse().getPlayers()[0];

        var p = new Profile(player.getSteamid(), player.getPersonaname(), player.getAvatarfull(), player.getProfileurl(),new ArrayList<>(), new ArrayList<>());
        log.info("Profile " + id + " is " + p.getNickname() + "(" + p.getId() + ")");
        return p;
    }


    @Override
    public String getGameName(String id) throws ExecutionException {
        return this.games.get().get(id);
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

        var profileFuture = threadPool.submit(() -> steamWebCache.profileData(id));
        var gameFuture = threadPool.submit(() -> steamWebCache.gameData(id));
        var friendFuture = threadPool.submit(() -> steamWebCache.friendData(id));

        SteamProfileData profile = null;
        SteamGameData game = null;
        SteamFriendData friends = null;
        try {
            profile = profileFuture.get();
            game = gameFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        try{
            friends = friendFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            log.info("Could not get friend list");
        }

        var player = profile.getResponse().getPlayers()[0];
        if (game.getResponse().getGames() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This user (" + id + ") does not exist or has no games");

        var games = game.getResponse().getGames().stream()
                .map(SteamGameData.SteamGameDataListItem::getAppid)
                .map(String::valueOf)
                .collect(Collectors.toList());

        var friendsList = friends == null ? new ArrayList<String>() : friends.getFriendslist().getFriends().stream()
                .map(SteamFriendData.SteamFriendDataItem::getSteamid)
                .collect(Collectors.toList());

        var p = new Profile(player.getSteamid(), player.getPersonaname(), player.getAvatarfull(), player.getProfileurl(), games, friendsList);
        log.info("Profile " + id + " is " + p.getNickname() + "(" + p.getId() + ")");
        return p;
    }
}
