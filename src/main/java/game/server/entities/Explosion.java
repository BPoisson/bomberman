package game.server.entities;

import game.Direction;
import global.Constants;

import java.awt.*;

public class Explosion extends Bomb {

    public Explosion(int x, int y, int propagation, Direction direction) {
        super(x, y);
        this.color = Color.ORANGE;
        this.timer = System.nanoTime() + Constants.HALF_SECOND_NANO;
        this.propagation = propagation;
        this.direction = direction;
    }
}
