package de.karlthebee.commongames.clients;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class Group {
    private final String id;
    private List<String> ids = new ArrayList<>();

    private long version;

    public synchronized void update() {
        this.version = System.currentTimeMillis();
        this.notifyAll();
    }

    public synchronized void waitForUpdate() throws InterruptedException {
        this.wait(60 * 1000);
    }

}
