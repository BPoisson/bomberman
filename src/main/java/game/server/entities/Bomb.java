package game.server.entities;

import engine.Entity;
import game.Direction;
import global.Constants;
import global.Coordinate;

import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Bomb extends Entity {
    public UUID playerUUID;
    public long timer;
    public int propagation;
    public Direction direction;

    public Bomb(UUID playerUUID, int x, int y) {
        this.uuid = UUID.randomUUID();
        this.playerUUID = playerUUID;
        this.x = x;
        this.y = y;
        this.color = Color.BLACK;
        this.timer = System.nanoTime() + Constants.BOMB_TIMER_NANO;
        this.propagation = 0;
        // Up, Down, Left, Right.
        this.direction = Direction.NONE;
    }

    public List<Explosion> propagate(List<Entity> gameEntities) {
        if (propagation == Constants.MAX_PROPAGATIONS) {
            return Collections.emptyList();
        }

        List<Explosion> explosions = new LinkedList<>();
        if (direction == Direction.NONE) {
            // Up, Down, Left, Right.
            int[][] dirs = new int[][] {{0, Constants.TILE_SIZE},{0, -Constants.TILE_SIZE}, {-Constants.TILE_SIZE, 0}, {Constants.TILE_SIZE, 0}};
            for (int i = 0; i < dirs.length; i++) {
                int[] dir = dirs[i];
                Coordinate explosionCoord = new Coordinate(x + dir[0], y + dir[1]);
                boolean isTouchingBlock = checkBlockCollision(explosionCoord, gameEntities);

                explosions.add(new Explosion(this.playerUUID, explosionCoord.x, explosionCoord.y, propagation + 1, isTouchingBlock, Direction.values()[i + 1]));
            }
        } else {
            switch (direction) {
                case Direction.UP -> {
                    Coordinate explosionCoord = new Coordinate(x, y + Constants.TILE_SIZE);
                    boolean isTouchingBlock = checkBlockCollision(explosionCoord, gameEntities);

                    explosions.add(new Explosion(this.playerUUID, explosionCoord.x, explosionCoord.y, propagation + 1, isTouchingBlock, direction));
                }
                case Direction.DOWN -> {
                    Coordinate explosionCoord = new Coordinate(x, y - Constants.TILE_SIZE);
                    boolean isTouchingBlock = checkBlockCollision(explosionCoord, gameEntities);

                    explosions.add(new Explosion(this.playerUUID, explosionCoord.x, explosionCoord.y, propagation + 1, isTouchingBlock, direction));
                }
                case Direction.LEFT -> {
                    Coordinate explosionCoord = new Coordinate(x - Constants.TILE_SIZE, y);
                    boolean isTouchingBlock = checkBlockCollision(explosionCoord, gameEntities);

                    explosions.add(new Explosion(this.playerUUID, explosionCoord.x, explosionCoord.y, propagation + 1, isTouchingBlock, direction));
                }
                case Direction.RIGHT -> {
                    Coordinate explosionCoord = new Coordinate(x + Constants.TILE_SIZE, y);
                    boolean isTouchingBlock = checkBlockCollision(explosionCoord, gameEntities);

                    explosions.add(new Explosion(this.playerUUID, explosionCoord.x, explosionCoord.y, propagation + 1, isTouchingBlock, direction));
                }
            }
        }
        return explosions;
    }

    private boolean checkBlockCollision(Coordinate coordinate, List<Entity> entities) {
        for (Entity entity : entities) {
            // Explosions don't go through Blocks.
            if (entity instanceof Block && this.checkCollision(coordinate, entity)) {
                return true;
            }
        }
        return false;
    }

    public List<Entity> checkExplodeCollision(List<Entity> gameEntities) {
        List<Entity> exploded = new LinkedList<>();

        for (Entity entity : gameEntities) {
            if (!(entity instanceof Block) && !(entity instanceof HealthPickup) && this.checkCollision(entity)) {
                exploded.add(entity);
            }
        }
        return exploded;
    }

    public boolean isExpired() {
        return timer <= System.nanoTime();
    }
}
