package game.client.entities;

import java.awt.*;
import java.util.UUID;

public class Explosion extends Bomb {

    public Explosion(UUID uuid, int x, int y) {
        super(uuid, x, y);
        this.color = Color.ORANGE;
    }
}
