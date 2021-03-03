package de.karlthebee.commongames.services;

import de.karlthebee.commongames.services.interfaces.GroupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GroupServiceImplTest {

    @Autowired
    private GroupService groupService;

    @Test
    void generateId() {
        var id = groupService.generateId();
        assertEquals(id.length(), 6);
    }
}
