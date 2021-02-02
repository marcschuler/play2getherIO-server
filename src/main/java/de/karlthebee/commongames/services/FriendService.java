package de.karlthebee.commongames.services;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.karlthebee.commongames.clients.Group;
import de.karlthebee.commongames.clients.Profile;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class FriendService {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);

    private final SteamDataService steamDataService;

    private final LoadingCache<Group, List<Profile>> profiles =
            CacheBuilder.newBuilder()
                    .expireAfterAccess(30, TimeUnit.MINUTES)
                    .build(new CacheLoader<>() {
                        @Override
                        public List<Profile> load(Group group) {
                            return fetchFriendList(group);
                        }
                    });

    /**
     * Removes a friend group from the cache.
     * Useful if new data should be given back to rest services
     *
     * @param group the group to delete
     */
    public void resetFriendGroup(Group group) {
        profiles.invalidate(group);
    }

    private List<Profile> fetchFriendList(Group group) {
        log.info("Updating suggestions of " + group.getId());
        var friends = findFriends(group);
        return friends;
    }

    /**
     * Gets the current suggestions from cache or calculates
     *
     * @param group the group
     * @return the recommended profiles
     * @throws ExecutionException if the suggestions throws an error
     */
    public @Nullable List<Profile> getCurrentSuggestions(Group group) throws ExecutionException {
        return profiles.get(group);
    }

    private List<Profile> findFriends(Group group) {
        List<Profile> collect = findCommonFriends(group).stream()
                .peek(f -> log.info("Finding friend data of " + f))
                .map(profile -> {
                    try {
                        return steamDataService.getProfile(profile);
                    } catch (Exception e) {
                        log.warn("Could not find friend " + profile);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .limit(15)
                .collect(Collectors.toList());
        log.info("Friends found. Returning");
        return collect;
    }

    public List<String> findCommonFriends(Group group) {
        var allfriends = group.getIds().stream()
                .limit(5) //Only the first 10 IDs to avoid API spamming
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

        var friendsSorted = new ArrayList<>(allfriends.entrySet());
        friendsSorted.sort(Map.Entry.comparingByValue());
        Collections.reverse(friendsSorted);

        var friends = friendsSorted.stream()
                .map(Map.Entry::getKey)
                .limit(30)
                .collect(Collectors.toList());
        log.info("Returing common friend list");
        return friends;
    }


}
