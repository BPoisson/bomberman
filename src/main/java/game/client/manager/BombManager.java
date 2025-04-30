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

    public void remove(UUID bombUUID) {
        if (!bombMap.containsKey(bombUUID)) {
            System.err.println("Unable to find bomb: " + bombUUID);
        }
        Bomb bomb = bombMap.get(bombUUID);
        bombList.remove(bomb);
        bombMap.remove(bombUUID);
    }

    public List<Bomb> getBombs() {
        return bombList;
    }
}
