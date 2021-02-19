package de.karlthebee.commongames.dto;


import de.karlthebee.commongames.model.Game;
import de.karlthebee.commongames.model.Profile;
import lombok.Data;

import java.util.Set;

@Data
public class WebDto {
    private String id;
    private long version;

    private Set<Profile> profiles;
    private Set<Game> games;

}
