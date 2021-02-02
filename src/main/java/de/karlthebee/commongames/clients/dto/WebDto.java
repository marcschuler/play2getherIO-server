package de.karlthebee.commongames.clients.dto;


import de.karlthebee.commongames.clients.Game;
import de.karlthebee.commongames.clients.Profile;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class WebDto {
    private String id;
    private long version;

    private Set<Profile> profiles;
    private Set<Game> games;

}
