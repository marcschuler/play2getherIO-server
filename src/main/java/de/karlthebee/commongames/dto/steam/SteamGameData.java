package de.karlthebee.commongames.dto.steam;

import lombok.Data;

import java.util.List;

@Data
public class SteamGameData {

    private SteamGameDataList response;

    @Data
    public static class SteamGameDataList {
        private int game_count; //int should be enough?
        private List<SteamGameDataListItem> games;
    }

    @Data
    public static class SteamGameDataListItem {
        private int appid;
        private int playtime_forever;
        private int playtime_windows_forever;
        private int playtime_mac_forever;
        private int playtime_linux_forever;
    }
}
