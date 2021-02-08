package de.karlthebee.commongames.rest;

import de.karlthebee.commongames.clients.Profile;
import de.karlthebee.commongames.clients.dto.WebDto;
import de.karlthebee.commongames.rest.dto.GroupSetupDto;
import de.karlthebee.commongames.services.DtoService;
import de.karlthebee.commongames.services.FriendService;
import de.karlthebee.commongames.services.GroupService;
import de.karlthebee.commongames.services.SteamDataService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("v1/groups")
@CrossOrigin
@Data
@Slf4j
public class GroupRest {

    private final SteamDataService steamDataService;
    private final GroupService groupService;
    private final DtoService dtoService;
    private final FriendService friendService;

    @Value("${groups.profiles.max}")
    private int groupProfilesMax;

    @PostMapping
    public WebDto createGroup(@RequestBody GroupSetupDto groupSetupDto) throws ExecutionException {
        var group = groupService.generateGroup();
        addFriend(group.getId(), groupSetupDto);
        friendService.resetFriendGroup(group); //not needed but recommended
        return dtoService.byId(group.getId());
    }

    @GetMapping("{gid}")
    public WebDto getState(@PathVariable("gid") String gid) {
        return dtoService.byId(gid);
    }

    @GetMapping("{gid}/live/{version}")
    public WebDto getLiveState(@PathVariable("gid") String gid, @PathVariable("version") long version) {
        var group = groupService.getGroup(gid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group does not exist"));
        if (group.getVersion() == version) {
            try {
                log.info("Waiting for group " + gid + "/" + version);
                group.waitForUpdate();
                log.info("Changes detected or timeout");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return dtoService.byId(gid);
    }

    @GetMapping("{gid}/friends/suggestions")
    public List<Profile> recommendedFriends(@PathVariable("gid") String gid) throws ExecutionException {
        var group = groupService.getGroup(gid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group does not exist"));
        return friendService.getCurrentSuggestions(group);
    }

    @PostMapping("{gid}/friends")
    public WebDto addFriend(@PathVariable("gid") String gid, @RequestBody() GroupSetupDto groupSetupDto) {
        Profile profile = null;
        try {
            profile = steamDataService.getProfile(groupSetupDto.getProfile());
        } catch (ExecutionException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find profile '" + groupSetupDto.getProfile() + "'");
        }

        try {
            var group = groupService.getGroup(gid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group does not exist"));

            if (group.getIds().size() + 1 == groupProfilesMax)
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot have more than 16 profiles");

            group.getIds().add(profile.getId());
            group.update();
            friendService.resetFriendGroup(group);
            log.info("Added profile " + profile.getNickname());
            return dtoService.byId(gid);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not execute request");
        }
    }

    @DeleteMapping("{gid}/friends/{fid}")
    public WebDto removeFriend(@PathVariable("gid") String gid, @PathVariable("fid") String fid) throws ExecutionException {
        var profile = steamDataService.getProfile(fid);
        var group = groupService.getGroup(gid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group does not exist"));

        if (group.getIds().size() <= 1)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not remove the last profile");

        var deleted = group.getIds().remove(profile.getId());
        if (!deleted)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user is not on the list. Please reload the page");
        group.update();
        friendService.resetFriendGroup(group);
        log.info("Deleted profile " + profile.getNickname());
        return dtoService.byId(gid);
    }
}
