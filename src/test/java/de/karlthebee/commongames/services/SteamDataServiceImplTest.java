package de.karlthebee.commongames.services;

import com.google.common.util.concurrent.UncheckedExecutionException;
import de.karlthebee.commongames.services.interfaces.SteamDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SteamDataServiceImplTest {

    @Autowired
    private SteamDataService steamDataService;

    @Test
    void getProfile() throws ExecutionException {
        assertThrows(ResponseStatusException.class,()->{
            steamDataService.getProfile("invalid-id");
        });

        var profile = steamDataService.getProfile("kArLtHeBeE");
        assertNotNull(profile);
        assertEquals("76561198065241640", profile.getId());
        assertNotNull(profile.getFriends());
        assertNotNull(profile.getOwnedGameIds());
    }

    @Test
    void getGameName() throws ExecutionException {
        assertEquals("Portal 2",steamDataService.getGameName("620"));
        assertEquals("Counter-Strike: Global Offensive",steamDataService.getGameName("730"));
        assertNull(steamDataService.getGameName(null));
        assertNull(steamDataService.getGameName("invalid"));
    }

    @Test
    void fetchProfileId() {
    }

    @Test
    void fetchGames() {
        steamDataService.fetchGames();
    }

    @Test
    void fetchProfile() {
    }
}
