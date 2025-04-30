package game.client.entities;

import engine.Entity;

import java.awt.*;
import java.util.UUID;

public class Bomb extends Entity {
    public UUID playerUUID;

    public Bomb(UUID uuid, UUID playerUUID, int x, int y) {
        this.uuid = uuid;
        this.playerUUID = playerUUID;
        this.x = x;
        this.y = y;
        this.color = Color.BLACK;
    }
}
