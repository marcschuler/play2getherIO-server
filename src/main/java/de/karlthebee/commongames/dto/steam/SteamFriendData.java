package de.karlthebee.commongames.dto.steam;

import lombok.Data;

import java.util.List;

@Data
public class SteamFriendData {

    private SteamFriendDataList friendslist;

    @Data
    public static class SteamFriendDataList {
        private List<SteamFriendDataItem> friends;
    }

    @Data
    public static class SteamFriendDataItem {
        private String steamid;
        private String relationship;
        private long friend_since;
    }
}
