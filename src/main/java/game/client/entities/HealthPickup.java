package game.client.entities;

import engine.Entity;

import java.awt.*;
import java.util.UUID;

public class HealthPickup extends Entity {

    public HealthPickup(UUID uuid, int x, int y) {
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.color = Color.getHSBColor(120 / 360f, 1.0f, 0.8f);
    }
}
