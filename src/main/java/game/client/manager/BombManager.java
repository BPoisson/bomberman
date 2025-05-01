package game.client.manager;

import game.client.entities.Bomb;

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

    public void remove(UUID uuid) {
        if (!bombMap.containsKey(uuid)) {
            System.err.println("Unable to find bomb: " + uuid);
        }
        Bomb bomb = bombMap.get(uuid);
        bombList.remove(bomb);
        bombMap.remove(uuid);
    }

    public Bomb get(UUID uuid) {
        if (!bombMap.containsKey(uuid)) {
            System.err.println("Unable to find bomb: " + uuid);
        }
        return bombMap.get(uuid);
    }

    public List<Bomb> getBombs() {
        return bombList;
    }
}
