package global;

import game.Direction;
import org.json.JSONObject;

import java.util.UUID;

public class JSONCreator {

    public static JSONObject move(UUID uuid, Direction dir) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(Constants.UUID, uuid);
        jsonObj.put(Constants.ACTION, Constants.MOVE);
        jsonObj.put(Constants.DIRECTION, dir);

        return jsonObj;
    }

    public static JSONObject bomb(UUID uuid) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(Constants.UUID, uuid);
        jsonObj.put(Constants.ACTION, Constants.BOMB);

        return jsonObj;
    }

    public static JSONObject coord(int x, int y) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(Constants.X, x);
        jsonObj.put(Constants.Y, y);

        return jsonObj;
    }

    public static JSONObject bombNotPlaced() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(Constants.BOMB_PLACED, false);

        return jsonObj;
    }

    public static JSONObject bombPlaced(int x, int y) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(Constants.BOMB_PLACED, true);
        jsonObj.put(Constants.X, x);
        jsonObj.put(Constants.Y, y);

        return jsonObj;
    }
}
