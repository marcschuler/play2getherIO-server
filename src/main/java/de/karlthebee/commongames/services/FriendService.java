package de.karlthebee.commongames.services;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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

    private final Cache<Group, List<Profile>> profiles =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .build();

    public void updateFriendGroupAsync(Group group) {
        log.info("Updating suggestions of " + group.getId());
        executorService.submit(() -> {
            try {
                var friends = findFriends(group);
                profiles.put(group, friends);
                log.info("Added " + friends.size() + " friends to suggestions");
                group.update();
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    public @Nullable List<Profile> getCurrentSuggestions(Group group) {
        return profiles.getIfPresent(group);
    }

    public List<Profile> findFriends(Group group) {
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
                .map(pid -> {
                    try {
                        return steamDataService.getProfile(pid);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .flatMap(profile -> profile.getFriends().stream())
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
