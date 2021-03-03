package de.karlthebee.commongames.services;

import de.karlthebee.commongames.model.Game;
import de.karlthebee.commongames.dto.WebDto;
import de.karlthebee.commongames.services.interfaces.DtoService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class DtoServiceImpl implements DtoService {

    private final GroupServiceImpl groupService;
    private final SteamDataServiceImpl steamDataService;
    private final FriendServiceImpl friendService;

    @Override
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
        })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        var games = result.getProfiles().stream()
                .flatMap(profile -> profile.getOwnedGameIds().stream())
                .map(gameid -> new Game(gameid, steamDataService.getGameName(gameid)))
                .collect(Collectors.toSet());
        result.setGames(games);

        return result;
    }
}
