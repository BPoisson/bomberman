package game.server.entities;

import engine.Entity;
import global.Constants;

import java.awt.*;

public class Bomb extends Entity {
    public long timer;

    public Bomb(int x, int y) {
        this.x = x;
        this.y = y;
        this.color = Color.BLACK;
        this.timer = System.nanoTime() + Constants.BOMB_TIMER_NANO;
    }
}
