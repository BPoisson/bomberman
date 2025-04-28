package game.server.entities;

import engine.Entity;
import game.Direction;
import global.Constants;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class Explosion extends Bomb {
    private long propagateTimer;
    private boolean propagated;

    public Explosion(int x, int y, int propagation, Direction direction) {
        super(x, y);
        this.color = Color.ORANGE;
        this.timer = System.nanoTime() + Constants.HALF_SECOND_NANO;
        this.propagation = propagation;
        this.direction = direction;
        this.propagateTimer = System.nanoTime()  + Constants.QUARTER_SECOND_NANO;
        this.propagated = false;
    }

    public List<Explosion> propagate(List<Entity> gameEntities) {
        if (!propagated && propagateTimer <= System.nanoTime()) {
            propagated = true;
            return super.propagate(gameEntities);
        }
        return Collections.emptyList();
    }
}
