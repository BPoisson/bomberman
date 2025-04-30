package game.server.entities;

import engine.Entity;
import game.Direction;
import global.Constants;
import global.Coordinate;

import java.awt.*;
import java.net.InetAddress;
import java.util.UUID;

public class Player extends Entity {
    private int health;
    private int speed;
    private long bombCooldown;
    private Direction direction;
    public InetAddress address;
    public int port;

    public Player() {
        this.uuid = UUID.randomUUID();
        this.x = 100;
        this.y = 100;
        this.bombCooldown = 0;
        this.color = Color.BLUE;
        this.health = 3;
        this.speed = Constants.PLAYER_SPEED;
        this.direction = Direction.RIGHT;
    }

    public Player(UUID uuid, int x, int y, InetAddress address, int port) {
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.bombCooldown = 0;
        this.color = Color.BLUE;
        this.health = 3;
        this.speed = Constants.PLAYER_SPEED;
        this.direction = Direction.RIGHT;
        this.address = address;
        this.port = port;
    }

    public Coordinate getNextPosition(Direction dir) {
        Coordinate coordinate = new Coordinate(x, y);

        switch (dir) {
            case Direction.UP:
                coordinate.y -= speed;
                direction = Direction.UP;
                break;
            case Direction.DOWN:
                coordinate.y += speed;
                direction = Direction.DOWN;
                break;
            case Direction.LEFT:
                coordinate.x -= speed;
                direction = Direction.LEFT;
                break;
            case Direction.RIGHT:
                coordinate.x += speed;
                direction = Direction.RIGHT;
                break;
            default:
        }
        return coordinate;
    }

    public void move(Direction dir) {
        switch (dir) {
            case Direction.UP:
                y -= speed;
                direction = Direction.UP;
                break;
            case Direction.DOWN:
                y += speed;
                direction = Direction.DOWN;
                break;
            case Direction.LEFT:
                x -= speed;
                direction = Direction.LEFT;
                break;
            case Direction.RIGHT:
                x += speed;
                direction = Direction.RIGHT;
                break;
            default:
        }
    }

    public Bomb placeBomb() {
        // Check bomb cooldown.
        if (System.nanoTime() < bombCooldown) {
            return null;
        }
        Bomb bomb = new Bomb(uuid, x, y);
        bombCooldown = System.nanoTime() + Constants.BOMB_COOLDOWN;

        return bomb;
    }

    public void decrementHealth() {
        health--;
    }
}
