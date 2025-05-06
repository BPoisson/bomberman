package game.server.entities;

import engine.Entity;

import java.awt.*;
import java.util.UUID;

public class Box extends Entity {
    boolean hasHealthPickup;

    public Box(int x, int y, boolean hasHealthPickup) {
        this.uuid = UUID.randomUUID();
        this.x = x;
        this.y = y;
        this.hasHealthPickup = hasHealthPickup;
        this.color = Color.getHSBColor(30 / 360f, 0.6f, 0.4f);
    }

    public boolean hasHealthPickup() {
        return hasHealthPickup;
    }
}
