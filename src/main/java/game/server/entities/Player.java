package game.server.entities;

import engine.Entity;
import game.Direction;
import global.Constants;

import java.awt.*;
import java.net.InetAddress;
import java.util.List;
import java.util.*;

public class Player extends Entity {
    private int speed;
    private long bombCooldown;
    private Direction direction;
    private List<Bomb> bombList;
    private Map<UUID, Bomb> bombMap;
    public InetAddress address;
    public int port;

    public Player() {
        this.uuid = UUID.randomUUID();
        this.x = 100;
        this.y = 100;
        this.bombCooldown = 0;
        this.color = Color.BLUE;
        this.speed = Constants.PLAYER_SPEED;
        this.direction = Direction.RIGHT;
        this.bombList = new LinkedList<>();
        this.bombMap = new HashMap<>();
    }

    public Player(UUID uuid, int x, int y, InetAddress address, int port) {
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.bombCooldown = 0;
        this.color = Color.BLUE;
        this.speed = Constants.PLAYER_SPEED;
        this.direction = Direction.RIGHT;
        this.bombList = new LinkedList<>();
        this.bombMap = new HashMap<>();
        this.address = address;
        this.port = port;
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
        if (System.nanoTime() < bombCooldown) {
            return null;
        }

        Bomb bomb;
        switch (direction) {
            case Direction.UP: {
                bomb = new Bomb(x, y - Constants.TILE_SIZE);
                break;
            }
            case Direction.DOWN: {
                bomb = new Bomb(x, y + Constants.TILE_SIZE);
                break;
            }
            case Direction.LEFT: {
                bomb = new Bomb(x - Constants.TILE_SIZE, y);
                break;
            }
            case Direction.RIGHT: {
                bomb = new Bomb(x + Constants.TILE_SIZE, y);
                break;
            }
            default:
                throw new IllegalArgumentException("Player has no direction. Can't place bomb");
        }
        bombList.add(bomb);
        bombMap.put(bomb.uuid, bomb);
        bombCooldown = System.nanoTime() + Constants.ONE_SECOND_NANO * 2;

        return bomb;
    }

    public List<Bomb> expireBombs() {
        List<Bomb> expiredBombs = new LinkedList<>();
        if (bombList.isEmpty()) {
            return expiredBombs;
        }

        final long currTime = System.nanoTime();
        for (Bomb bomb : bombList) {
            if (bomb.timer <= currTime) {
                expiredBombs.add(bomb);
                bombMap.remove(bomb.uuid);
            }
        }
        bombList.removeAll(expiredBombs);
        return expiredBombs;
    }
}
