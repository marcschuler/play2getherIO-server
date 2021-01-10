package de.karlthebee.commongames.services.dto;

import lombok.Data;

@Data
public class SteamIdData {
    private SteamIdDataResponse response;

    @Data
    public class SteamIdDataResponse {
        private int success; //1=success, 42=not
        private String steamid;
        private String message;
    }
}
