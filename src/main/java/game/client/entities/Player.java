package game.client.entities;

import engine.Entity;
import global.Constants;

import java.awt.*;
import java.util.UUID;

public class Player extends Entity {
    private int health;
    private long bombCooldown;
    private boolean isImmune;

    public Player() {
        this.uuid = UUID.randomUUID();
        this.x = -100;
        this.y = -100;
        this.color = Color.getHSBColor(210 / 360f, 1.0f, 0.8f);
        this.health = 3;
        this.bombCooldown = 0;
        this.isImmune = false;
    }

    public Player(UUID uuid, int x, int y) {
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.color = Color.getHSBColor(0 / 360f, 1.0f, 0.7f);
        this.health = 3;
        this.isImmune = false;
    }

    public void handleHit() {
        health--;
        isImmune = true;
    }

    public int getHealth() {
        return health;
    }

    public boolean isBombOnCooldown() {
        return System.nanoTime() < this.bombCooldown;
    }

    public void setBombCooldown() {
        this.bombCooldown = System.nanoTime() + Constants.BOMB_COOLDOWN;
    }

    public boolean isImmune() {
        return isImmune;
    }

    public void disableImmunity() {
        isImmune = false;
    }
}
