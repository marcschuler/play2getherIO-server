package de.karlthebee.commongames.services.dto;

import lombok.Data;

import java.util.List;

@Data
public class SteamAppData {
    private SteamAppList applist;

    @Data
    public static class SteamAppList{
        private List<SteamAppListItem> apps;
    }

    @Data
    public static class SteamAppListItem{
        private String appid;
        private String name;

    }
}
