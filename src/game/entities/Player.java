package game.entities;

import game.Direction;
import game.constants.Constants;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class Player extends Entity {
    public int speed;
    public boolean canPlaceBomb;
    public Direction direction;
    public List<Bomb> bombList;

    public Player() {
        this.x = 100;
        this.y = 100;
        this.canPlaceBomb = true;
        this.color = Color.WHITE;
        this.speed = Constants.PLAYER_SPEED;
        this.direction = Direction.RIGHT;
        this.bombList = new LinkedList<>();
    }

    public void expireBombs() {
        if (bombList.isEmpty()) {
            return;
        }

        final long currTime = System.currentTimeMillis();
        bombList.removeIf(bomb -> bomb.timer <= currTime);
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}
