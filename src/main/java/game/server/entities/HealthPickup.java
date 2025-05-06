package game.server.entities;

import engine.Entity;

import java.util.UUID;

public class HealthPickup extends Entity {

    public HealthPickup(int x, int y) {
        this.uuid = UUID.randomUUID();
        this.x = x;
        this.y = y;
    }
}
