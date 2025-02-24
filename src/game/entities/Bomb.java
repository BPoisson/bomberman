package game.entities;

import game.constants.Constants;

import java.awt.*;

public class Bomb extends Entity {
    public long timer;

    public Bomb(int x, int y) {
        this.x = x;
        this.y = y;
        this.color = Color.BLUE;
        this.timer = System.currentTimeMillis() + Constants.BOMB_TIMER_MILLIS;
    }
}
