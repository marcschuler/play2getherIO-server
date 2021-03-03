package de.karlthebee.commongames.services;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.karlthebee.commongames.model.Group;
import de.karlthebee.commongames.model.Profile;
import de.karlthebee.commongames.services.interfaces.FriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);

    private final SteamDataServiceImpl steamDataService;

    @Value("${groups.suggestions.max}")
    private int suggestionsMax;

    private final LoadingCache<Group, List<Profile>> profiles =
            CacheBuilder.newBuilder()
                    .expireAfterAccess(30, TimeUnit.MINUTES)
                    .build(new CacheLoader<>() {
                        @SuppressWarnings("NullableProblems")
                        @Override
                        public List<Profile> load(Group group) {
                            return fetchFriendList(group);
                        }
                    });

    @Override
    public void resetFriendGroup(Group group) {
        profiles.invalidate(group);
    }

    @Override
    public List<Profile> fetchFriendList(Group group) {
        log.info("Updating suggestions of " + group.getId());
        var friends = findFriends(group);
        return friends;
    }

    @Override
    public @Nullable List<Profile> getCurrentSuggestions(Group group) throws ExecutionException {
        return profiles.get(group);
    }

    @Override
    public List<Profile> findFriends(Group group) {
        List<Profile> collect = findCommonFriends(group).parallelStream()
                .peek(f -> log.info("Finding friend data of " + f))
                .map(profile -> {
                    try {
                        return steamDataService.getEmptyFriendProfile(profile);
                    } catch (Exception e) {
                        log.warn("Could not find friend " + profile);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .limit(suggestionsMax)
                .collect(Collectors.toList());
        log.info("Friends found. Returning");
        return collect;
    }

    @Override
    public List<String> findCommonFriends(Group group) {
        var allfriends = group.getIds().stream()
                .limit(8) //Only the first 8 IDs to avoid API spamming
                .map(pid -> {
                    try {
                        return steamDataService.getProfile(pid);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .flatMap(profile -> profile.getFriends().stream())
                .filter(profile -> !group.getIds().contains(profile))   //Remove existing profiles
                .collect(Collectors.groupingBy(p -> p, Collectors.counting()));

        //Use the most commonly named friends
        var friendsSorted = new ArrayList<>(allfriends.entrySet());
        friendsSorted.sort(Map.Entry.comparingByValue());
        Collections.reverse(friendsSorted);

        var friends = friendsSorted.stream()
                .map(Map.Entry::getKey)
                .limit(suggestionsMax)
                .collect(Collectors.toList());
        log.info("Returing common friend list");
        return friends;
    }


}
