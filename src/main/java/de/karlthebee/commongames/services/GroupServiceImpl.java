package de.karlthebee.commongames.services;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.karlthebee.commongames.model.Group;
import de.karlthebee.commongames.services.interfaces.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class GroupServiceImpl implements GroupService {

    private static final char[] ID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    private static final Cache<String, Group> groups = CacheBuilder
            .newBuilder()
            .expireAfterAccess(12, TimeUnit.HOURS)
            .build();

    @Override
    public synchronized Group generateGroup() {
        var id = generateId();
        var group = new Group(id);
        group.setVersion(System.currentTimeMillis());
        groups.put(id, group);
        log.info("Generating group " + id);
        return group;
    }

    @Override
    public Optional<Group> getGroup(String id) {
        return Optional.ofNullable(groups.getIfPresent(id));
    }

    @Override
    public String generateId() {
        var length = Math.max(6, (int) Math.ceil(Math.log10(groups.size()) + 1)); //Enough length for everybody
        StringBuilder id;
        do {
            id = new StringBuilder();
            for (int n = 0; n < length; n++) {
                id.append(ID_CHARS[(int) (Math.random() * ID_CHARS.length)]);
            }
        } while (groups.getIfPresent(id.toString()) != null);

        return id.toString();
    }
}
