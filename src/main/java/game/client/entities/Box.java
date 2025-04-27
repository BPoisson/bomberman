package game.client.entities;

import engine.Entity;

import java.awt.*;
import java.util.UUID;

public class Box extends Entity {

    public Box(UUID uuid, int x, int y) {
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.color = Color.getHSBColor(30 / 360f, 0.6f, 0.4f);
    }
}
