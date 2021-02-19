package de.karlthebee.commongames.model;

import lombok.Data;

import java.util.List;

@Data
public class Profile {

    private final String id;
    private final String nickname;
    private final String profileImageUrl;
    private final String profileUrl;
    private final List<String> ownedGameIds;

    private final List<String> friends;

}
