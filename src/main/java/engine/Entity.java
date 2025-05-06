package engine;

import game.server.entities.HealthPickup;
import game.server.entities.Player;
import global.Constants;
import global.Coordinate;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public abstract class Entity {
    public UUID uuid;
    public int x;
    public int y;
    public Color color;
    
    public boolean checkCollision(Entity entity) {
        if (entity instanceof Player && this instanceof Player && entity.uuid.equals(this.uuid)) {
            return false;
        }

        int xMin = this.x + 1;
        int yMin = this.y + 1;
        int xMax = this.x + Constants.TILE_SIZE - 1;
        int yMax = this.y + Constants.TILE_SIZE - 1;
        int entityXMin = entity.x;
        int entityYMin = entity.y;
        int entityXMax = entity.x + Constants.TILE_SIZE;
        int entityYMax = entity.y + Constants.TILE_SIZE;

        if ((entityXMin <= xMax && xMax <= entityXMax) || (entityXMin <= xMin && xMin <= entityXMax)) {
            if ((entityYMin <= yMax && yMax <= entityYMax) || (entityYMin <= yMin && yMin <= entityYMax)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkCollision(Coordinate coordinate, Entity entity) {
        if (entity instanceof Player && this instanceof Player && entity.uuid.equals(this.uuid)) {
            return false;
        }

        int xMin = coordinate.x + 1;
        int yMin = coordinate.y + 1;
        int xMax = coordinate.x + Constants.TILE_SIZE - 1;
        int yMax = coordinate.y + Constants.TILE_SIZE - 1;
        int entityXMin = entity.x;
        int entityYMin = entity.y;
        int entityXMax = entity.x + Constants.TILE_SIZE;
        int entityYMax = entity.y + Constants.TILE_SIZE;

        if ((entityXMin <= xMax && xMax <= entityXMax) || (entityXMin <= xMin && xMin <= entityXMax)) {
            if ((entityYMin <= yMax && yMax <= entityYMax) || (entityYMin <= yMin && yMin <= entityYMax)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkCollision(List<Entity> entities) {
        int xMin = this.x + 1;
        int yMin = this.y + 1;
        int xMax = this.x + Constants.TILE_SIZE - 1;
        int yMax = this.y + Constants.TILE_SIZE - 1;

        for (Entity entity : entities) {
            if (entity instanceof Player && this instanceof Player && entity.uuid.equals(this.uuid)) {
                continue;
            }

            int entityXMin = entity.x;
            int entityYMin = entity.y;
            int entityXMax = entity.x + Constants.TILE_SIZE;
            int entityYMax = entity.y + Constants.TILE_SIZE;

            if ((entityXMin <= xMax && xMax <= entityXMax) || (entityXMin <= xMin && xMin <= entityXMax)) {
                if ((entityYMin <= yMax && yMax <= entityYMax) || (entityYMin <= yMin && yMin <= entityYMax)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkCollision(Coordinate coordinate, List<Entity> entities) {
        int xMin = coordinate.x + 1;
        int yMin = coordinate.y + 1;
        int xMax = coordinate.x + Constants.TILE_SIZE - 1;
        int yMax = coordinate.y + Constants.TILE_SIZE - 1;

        for (Entity entity : entities) {
            if (entity instanceof HealthPickup || (entity instanceof Player && this instanceof Player && entity.uuid.equals(this.uuid))) {
                continue;
            }

            int entityXMin = entity.x;
            int entityYMin = entity.y;
            int entityXMax = entity.x + Constants.TILE_SIZE;
            int entityYMax = entity.y + Constants.TILE_SIZE;

            if ((entityXMin <= xMax && xMax <= entityXMax) || (entityXMin <= xMin && xMin <= entityXMax)) {
                if ((entityYMin <= yMax && yMax <= entityYMax) || (entityYMin <= yMin && yMin <= entityYMax)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<HealthPickup> checkHealthCollision(List<Entity> entities) {
        int xMin = this.x + 1;
        int yMin = this.y + 1;
        int xMax = this.x + Constants.TILE_SIZE - 1;
        int yMax = this.y + Constants.TILE_SIZE - 1;
        List<HealthPickup> collidedHealth = new LinkedList<>();

        for (Entity entity : entities) {
            if (!(entity instanceof HealthPickup)) {
                continue;
            }

            int entityXMin = entity.x;
            int entityYMin = entity.y;
            int entityXMax = entity.x + Constants.TILE_SIZE;
            int entityYMax = entity.y + Constants.TILE_SIZE;

            if ((entityXMin <= xMax && xMax <= entityXMax) || (entityXMin <= xMin && xMin <= entityXMax)) {
                if ((entityYMin <= yMax && yMax <= entityYMax) || (entityYMin <= yMin && yMin <= entityYMax)) {
                    collidedHealth.add((HealthPickup) entity);
                }
            }
        }
        return collidedHealth;
    }
}
