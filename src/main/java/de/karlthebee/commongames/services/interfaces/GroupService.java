package de.karlthebee.commongames.services.interfaces;

import de.karlthebee.commongames.clients.Group;

import java.util.Optional;

public interface GroupService {
    Group generateGroup();

    Optional<Group> getGroup(String id);

    /**
     * Generates an random group id
     * @return the random id
     */
    String generateId();
}
