package de.karlthebee.commongames.rest;

import de.karlthebee.commongames.dto.GroupSetupDto;
import de.karlthebee.commongames.dto.WebDto;
import de.karlthebee.commongames.services.SteamDataServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GroupRestTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void before(){
    }

    private String getUrl() {
        return "http://localhost:" + port + "/v1/groups";
    }

    @Test
    void createGroup() {
        var setup = new GroupSetupDto();
        setup.setProfile("https://steamcommunity.com/profiles/76561198071367462/");
        var dto = restTemplate.postForEntity(getUrl(), setup, WebDto.class);
        assertNotNull(dto);
        assertTrue(dto.getStatusCode().is2xxSuccessful());
        var body = dto.getBody();
        assertNotNull(dto.getBody().getId());
    }

    @Test
    void getState() {
    }

    @Test
    void getLiveState() {
    }

    @Test
    void recommendedFriends() {
    }

    @Test
    void addFriend() {
    }

    @Test
    void removeFriend() {
    }

    @Test
    void getSteamDataService() {
    }

    @Test
    void getGroupService() {
    }

    @Test
    void getDtoService() {
    }

    @Test
    void getFriendService() {
    }

    @Test
    void getGroupProfilesMax() {
    }

    @Test
    void setGroupProfilesMax() {
    }
}
