package de.karlthebee.commongames.dto.steam;

import lombok.Data;

@Data
public class SteamProfileData {
    private SteamProfileResponseData response;

    @Data
    public static class SteamProfileResponseData {
        SteamProfileResponsePlayer[] players;
    }

    @Data
    public static class SteamProfileResponsePlayer {
        private String steamid;
        private int communityvisibilitystate;
        private int profilestate;
        private String personaname;
        private String profileurl;
        private String avatar;
        private String avatarmedium;
        private String avatarfull;
        private String avatarhash;
        private int personastate;
        private String realname;
        private String primaryclanid;
        private long timecreated;
        private int personastateflags;
        private String loccountrycode;
        private String locstatecode;
        private int loccityid;
    }

}

