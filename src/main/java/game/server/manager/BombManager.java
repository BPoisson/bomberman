package game.server.manager;

import engine.Entity;
import game.server.entities.Bomb;
import game.server.entities.Explosion;

import java.util.*;

public class BombManager {
    private List<Bomb> bombList;
    private Map<UUID, Bomb> bombMap;

    public BombManager() {
        bombList = new LinkedList<>();
        bombMap = new HashMap<>();
    }

    public void add(Bomb bomb) {
        bombList.add(bomb);
        bombMap.put(bomb.uuid, bomb);
    }

    public void addAll(List<Bomb> bombs) {
        for (Bomb bomb : bombs) {
            this.add(bomb);
        }
    }

    public void remove(UUID bombUUID) {
        if (!bombMap.containsKey(bombUUID)) {
            System.err.println("Unable to find bomb: " + bombUUID);
        }
        Bomb bomb = bombMap.get(bombUUID);
        bombList.remove(bomb);
        bombMap.remove(bombUUID);
    }

    public void removeAll(List<Bomb> bombs) {
        for (Bomb bomb : bombs) {
            this.remove(bomb.uuid);
        }
    }

    public List<Bomb> expireBombs() {
        List<Bomb> expiredBombs = new LinkedList<>();

        if (this.isEmpty()) {
            return expiredBombs;
        }

        for (Bomb bomb : bombList) {
            if (bomb.isExpired()) {
                expiredBombs.add(bomb);
            }
        }
        this.removeAll(expiredBombs);

        return expiredBombs;
    }

    public List<Bomb> propagateExplosions(List<Bomb> expiredBombs, List<Entity> gameEntities) {
        List<Bomb> propagated = new LinkedList<>();
        List<Bomb> explosions = new LinkedList<>(expiredBombs);
        explosions.addAll(this.getExplosions());

        for (Bomb explosion : explosions) {
            propagated.addAll(explosion.propagate(gameEntities));
        }
        this.addAll(propagated);
        return propagated;
    }

    public List<Entity> handleExplosionCollisions(List<Entity> gameEntities) {
        List<Explosion> explosions = this.getExplosions();
        List<Entity> explodedEntities = new LinkedList<>();

        for (Explosion explosion : explosions) {
            explodedEntities.addAll(explosion.checkExplodeCollision(gameEntities));
        }
        return explodedEntities;
    }

    private List<Explosion> getExplosions() {
        List<Explosion> explosions = new LinkedList<>();

        for (Bomb bomb : bombList) {
            if (bomb instanceof Explosion) {
                explosions.add((Explosion) bomb);
            }
        }
        return explosions;
    }

    private boolean isEmpty() {
        return bombList.isEmpty();
    }
}
