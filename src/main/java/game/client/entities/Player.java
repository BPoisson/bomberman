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
        this.color = Color.getHSBColor(210 / 360f, 1.0f, 0.8f);
        this.bombList = new LinkedList<>();
        this.bombMap = new HashMap<>();
    }

    public Player(UUID uuid, int x, int y) {
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.color = Color.getHSBColor(0 / 360f, 1.0f, 0.7f);
        this.bombList = new LinkedList<>();
        this.bombMap = new HashMap<>();
    }
}
