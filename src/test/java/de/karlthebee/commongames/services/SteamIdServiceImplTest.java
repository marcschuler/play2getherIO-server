package de.karlthebee.commongames.services;

import de.karlthebee.commongames.services.interfaces.SteamIdService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SteamIdServiceImplTest {

    @Autowired
    private SteamIdService steamIdService;

    @Test
    void prepareId() {
        assertEquals("KarlTheBee", steamIdService.prepareId("https://steamcommunity.com/id/KarlTheBee"));
        assertEquals("KarlTheBee", steamIdService.prepareId("https://steamcommunity.com/id/KarlTheBee/"));
        assertEquals("KarlTheBee", steamIdService.prepareId("http://www.steamcommunity.com/id/KarlTheBee/"));
        assertEquals("76561198065241640", steamIdService.prepareId("https://steamcommunity.com/profiles/76561198065241640/"));
    }

    @Test
    void isId64() {
        assertTrue(steamIdService.isId64("76561198065241640"));
        assertTrue(steamIdService.isId64("0000000000"));
        assertFalse(steamIdService.isId64("k"));
        assertFalse(steamIdService.isId64("123456789kkkk"));
        assertTrue(steamIdService.isId64("https://steamcommunity.com/profiles/76561198065241640/"));
    }

    @Test
    void isValidId() {
        assertTrue(steamIdService.isValidId("76561198065241640"));
        assertFalse(steamIdService.isValidId("0000000000"));
        assertTrue(steamIdService.isValidId("k"));
        assertTrue(steamIdService.isValidId("testtest"));
        assertFalse(steamIdService.isValidId("testtest-"));
        assertTrue(steamIdService.isValidId("https://steamcommunity.com/profiles/76561198065241640/"));
    }
}
