package de.karlthebee.commongames.services.interfaces;

import de.karlthebee.commongames.clients.Group;
import de.karlthebee.commongames.clients.Profile;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface FriendService {
    /**
     * Removes a friend group from the cache.
     * Useful if new data should be given back to rest services
     *
     * @param group the group to delete
     */
    void resetFriendGroup(Group group);

    List<Profile> fetchFriendList(Group group);

    /**
     * Gets the current suggestions from cache or calculates
     *
     * @param group the group
     * @return the recommended profiles
     * @throws ExecutionException if the suggestions throws an error
     */
    @Nullable List<Profile> getCurrentSuggestions(Group group) throws ExecutionException;

    List<Profile> findFriends(Group group);

    List<String> findCommonFriends(Group group);
}
