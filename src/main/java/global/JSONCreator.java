package global;

import engine.Entity;
import game.Direction;
import game.server.entities.Block;
import game.server.entities.Box;
import game.server.entities.GameMap;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class JSONCreator {

    public static JSONObject createGameMapJson(GameMap gameMap) {
        JSONObject jsonObject = new JSONObject();
        List<JSONObject> gameMapEntities = new LinkedList<>();

        for (Entity e : gameMap.mapEntities) {
            if (e instanceof Block) {
                gameMapEntities.add(JSONCreator.block(e.uuid, e.x, e.y));
            } else if (e instanceof Box) {
                gameMapEntities.add(JSONCreator.box(e.uuid, e.x, e.y));
            } else {
                throw new RuntimeException("Unexpected game map entity.");
            }
        }
        jsonObject.put(Constants.GAME_MAP, gameMapEntities);

        return jsonObject;
    }

    public static JSONObject move(UUID uuid, Direction dir) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(Constants.PLAYER_UUID, uuid);
        jsonObj.put(Constants.ACTION, Constants.MOVE);
        jsonObj.put(Constants.DIRECTION, dir);

        return jsonObj;
    }

    public static JSONObject block(UUID uuid, int x, int y) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(Constants.ENTITY, Constants.BLOCK);
        jsonObject.put(Constants.UUID, uuid);
        jsonObject.put(Constants.X, x);
        jsonObject.put(Constants.Y, y);

        return jsonObject;
    }

    public static JSONObject box(UUID uuid, int x, int y) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(Constants.ENTITY, Constants.BOX);
        jsonObject.put(Constants.UUID, uuid);
        jsonObject.put(Constants.X, x);
        jsonObject.put(Constants.Y, y);

        return jsonObject;
    }

    public static JSONObject bomb(UUID uuid) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(Constants.PLAYER_UUID, uuid);
        jsonObj.put(Constants.ACTION, Constants.BOMB);

        return jsonObj;
    }

    public static JSONObject playerMoved(UUID uuid, int x, int y) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(Constants.ACTION, Constants.MOVE);
        jsonObj.put(Constants.PLAYER_UUID, uuid);
        jsonObj.put(Constants.X, x);
        jsonObj.put(Constants.Y, y);

        return jsonObj;
    }

    public static JSONObject bombNotPlaced() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(Constants.ACTION, Constants.BOMB);
        jsonObj.put(Constants.BOMB_PLACED, false);

        return jsonObj;
    }

    public static JSONObject bombPlaced(UUID playerUUID, UUID bombUUID, int x, int y) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(Constants.ACTION, Constants.BOMB);
        jsonObj.put(Constants.BOMB_PLACED, true);
        jsonObj.put(Constants.PLAYER_UUID, playerUUID);
        jsonObj.put(Constants.BOMB_UUID, bombUUID);
        jsonObj.put(Constants.X, x);
        jsonObj.put(Constants.Y, y);

        return jsonObj;
    }

    public static JSONObject bombExpired(UUID playerUUID, UUID bombUUID) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(Constants.ACTION, Constants.BOMB_EXPIRED);
        jsonObj.put(Constants.PLAYER_UUID, playerUUID);
        jsonObj.put(Constants.BOMB_UUID, bombUUID);

        return jsonObj;
    }

    public static JSONObject getGameMap(UUID playerUUID) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(Constants.PLAYER_UUID, playerUUID);

        return jsonObj;
    }

    public static JSONObject registerPlayer(UUID uuid) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(Constants.PLAYER_UUID, uuid);

        return jsonObj;
    }

    public static JSONObject playerAck(int x, int y) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(Constants.ACK, true);
        jsonObj.put(Constants.X, x);
        jsonObj.put(Constants.Y, y);

        return jsonObj;
    }

    public static JSONObject gameStart(UUID uuid, int x, int y) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(Constants.START, true);
        jsonObj.put(Constants.PLAYER_UUID, uuid);
        jsonObj.put(Constants.X, x);
        jsonObj.put(Constants.Y, y);

        return jsonObj;
    }
}
