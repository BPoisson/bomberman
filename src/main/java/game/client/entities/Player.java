package game.client.entities;

import engine.Entity;

import java.awt.*;
import java.util.UUID;

public class Player extends Entity {
    private int health;

    public Player() {
        this.uuid = UUID.randomUUID();
        this.x = -100;
        this.y = -100;
        this.color = Color.getHSBColor(210 / 360f, 1.0f, 0.8f);
        this.health = 3;
    }

    public Player(UUID uuid, int x, int y) {
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.color = Color.getHSBColor(0 / 360f, 1.0f, 0.7f);
        this.health = 3;
    }
}
