package de.karlthebee.commongames.rest;

import de.karlthebee.commongames.clients.dto.WebDto;
import de.karlthebee.commongames.rest.dto.GroupSetupDto;
import de.karlthebee.commongames.services.DtoService;
import de.karlthebee.commongames.services.GroupService;
import de.karlthebee.commongames.services.SteamDataService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @PostMapping
    public WebDto createGroup(@RequestBody GroupSetupDto groupSetupDto) throws ExecutionException {
        var group = groupService.generateGroup();
        addFriend(group.getId(), groupSetupDto);
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

    @PostMapping("{gid}/friends")
    public WebDto addFriend(@PathVariable("gid") String gid, @RequestBody() GroupSetupDto groupSetupDto) throws ExecutionException {
        var profile = steamDataService.getProfile(groupSetupDto.getProfile());
        var group = groupService.getGroup(gid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group does not exist"));
        group.getIds().add(profile.getId());
        group.update();
        log.info("Added profile " + profile.getNickname());
        return dtoService.byId(gid);
    }

    @DeleteMapping("{gid}/friends/{fid}")
    public WebDto removeFriend(@PathVariable("gid") String gid, @PathVariable("fid") String fid) throws ExecutionException {
        var profile = steamDataService.getProfile(fid);
        var group = groupService.getGroup(gid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group does not exist"));
        var deleted = group.getIds().remove(profile.getId());
        if (!deleted)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user is not on the list. Please reload the page");
        group.update();
        log.info("Deleted profile " + profile.getNickname());
        return dtoService.byId(gid);
    }
}
