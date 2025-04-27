package game.client.entities;

import engine.Entity;

import java.awt.*;
import java.util.UUID;

public class Block extends Entity {

    public Block(UUID uuid, int x, int y) {
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.color = Color.getHSBColor(0 / 360f, 0f, 0.4f);
    }
}
