package game.client.entities;

import engine.Entity;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Player extends Entity {
    public UUID uuid;
    public List<Bomb> bombList;

    public Player() {
        this.uuid = UUID.randomUUID();
        this.x = -100;
        this.y = -100;
        this.color = Color.BLUE;
        this.bombList = new LinkedList<>();
    }

    public Player(UUID uuid, int x, int y) {
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.color = Color.RED;
        this.bombList = new LinkedList<>();
    }

    public void expireBombs() {
        if (this.bombList.isEmpty()) {
            return;
        }

        final long currTime = System.nanoTime();
        this.bombList.removeIf(bomb -> bomb.timer <= currTime);
    }
}
