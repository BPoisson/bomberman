package game.client.entities;

import engine.Entity;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Player extends Entity {
    public List<Bomb> bombList;
    public Map<UUID, Bomb> bombMap;

    public Player() {
        this.uuid = UUID.randomUUID();
        this.x = -100;
        this.y = -100;
        this.color = Color.BLUE;
        this.bombList = new LinkedList<>();
        this.bombMap = new HashMap<>();
    }

    public Player(UUID uuid, int x, int y) {
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.color = Color.RED;
        this.bombList = new LinkedList<>();
        this.bombMap = new HashMap<>();
    }
}
