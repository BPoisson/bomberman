package game.server.entities;

import engine.Entity;
import game.Direction;
import global.Constants;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Player extends Entity {
    public UUID uuid;
    private int speed;
    private long bombCooldown;
    private Direction direction;
    private List<Bomb> bombList;

    public Player() {
        this.uuid = UUID.randomUUID();
        this.x = 100;
        this.y = 100;
        this.bombCooldown = 0;
        this.color = Color.BLUE;
        this.speed = Constants.PLAYER_SPEED;
        this.direction = Direction.RIGHT;
        this.bombList = new LinkedList<>();
    }

    public void move(String dir) {
        switch (dir) {
            case "UP":
                this.y -= this.speed;
                this.direction = Direction.UP;
                break;
            case "DOWN":
                this.y += this.speed;
                this.direction = Direction.DOWN;
                break;
            case "LEFT":
                this.x -= this.speed;
                this.direction = Direction.LEFT;
                break;
            case "RIGHT":
                this.x += this.speed;
                this.direction = Direction.RIGHT;
                break;
            default:
        }
    }

    public int[] placeBomb() {
        if (System.nanoTime() < bombCooldown) {
            return null;
        }

        Bomb bomb;
        switch (this.direction) {
            case Direction.UP: {
                bomb = new Bomb(this.x, this.y - Constants.TILE_SIZE);
                break;
            }
            case Direction.DOWN: {
                bomb = new Bomb(this.x, this.y + Constants.TILE_SIZE);
                break;
            }
            case Direction.LEFT: {
                bomb = new Bomb(this.x - Constants.TILE_SIZE, this.y);
                break;
            }
            case Direction.RIGHT: {
                bomb = new Bomb(this.x + Constants.TILE_SIZE, this.y);
                break;
            }
            default:
                throw new IllegalArgumentException("Player has no direction. Can't place bomb");
        }
        this.bombList.add(bomb);
        this.bombCooldown = System.nanoTime() + Constants.ONE_SECOND_NANO * 2;

        return new int[] {bomb.x, bomb.y};
    }

    public void expireBombs() {
        if (this.bombList.isEmpty()) {
            return;
        }

        final long currTime = System.nanoTime();
        this.bombList.removeIf(bomb -> bomb.timer <= currTime);
    }
}
