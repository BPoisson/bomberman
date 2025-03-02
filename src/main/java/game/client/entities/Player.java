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
        this.x = 100;
        this.y = 100;
        this.color = Color.WHITE;
        this.bombList = new LinkedList<>();
    }
}
