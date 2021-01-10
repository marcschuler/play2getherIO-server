package de.karlthebee.commongames.services;

import de.karlthebee.commongames.clients.Game;
import de.karlthebee.commongames.clients.dto.WebDto;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DtoService {

    private final GroupService groupService;
    private final SteamDataService steamDataService;

    public WebDto byId(String id) {
        var groupOptional = groupService.getGroup(id);
        if (groupOptional.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group '" + id + "' does not exist (anymore)");
        var group = groupOptional.get();

        var result = new WebDto();
        result.setId(group.getId());
        result.setVersion(group.getVersion());
        result.setProfiles(group.getIds().stream().map(pid -> {
            try {
                return steamDataService.getProfile(pid);
            } catch (ExecutionException e) {
                e.printStackTrace();
                return null;
            }
        }).collect(Collectors.toSet()));

        var games = result.getProfiles().stream()
                .flatMap(profile -> profile.getOwnedGameIds().stream())
                .map(gameid -> {
                    try {
                        return new Game(gameid, steamDataService.getGameName(gameid));
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .collect(Collectors.toSet());
        result.setGames(games);

        return result;
    }
}
