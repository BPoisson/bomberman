package game.server.entities;

import engine.Entity;
import game.Direction;
import global.Constants;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Explosion extends Bomb {
    private long propagateTimer;
    private boolean propagated;
    public boolean isTouchingBlock;

    public Explosion(UUID playerUUID, int x, int y, int propagation, boolean isTouchingBlock, Direction direction) {
        super(playerUUID, x, y);
        this.color = Color.ORANGE;
        this.timer = System.nanoTime() + Constants.HALF_SECOND_NANO;
        this.propagation = propagation;
        this.direction = direction;
        this.propagateTimer = System.nanoTime()  + Constants.QUARTER_SECOND_NANO;
        this.propagated = false;
        this.isTouchingBlock = isTouchingBlock;
    }

    public List<Explosion> propagate(List<Entity> gameEntities) {
        if (!isTouchingBlock && !propagated && propagateTimer <= System.nanoTime()) {
            propagated = true;
            return super.propagate(gameEntities);
        }
        return Collections.emptyList();
    }
}
