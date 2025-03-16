package game.client.entities;

import engine.Entity;
import global.Constants;

import java.awt.*;
import java.util.UUID;

public class Bomb extends Entity {
    public long timer;

    public Bomb(UUID uuid, int x, int y) {
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.color = Color.BLACK;
        this.timer = System.nanoTime() + Constants.BOMB_TIMER_NANO;
    }
}
