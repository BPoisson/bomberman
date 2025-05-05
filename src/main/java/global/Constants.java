package global;

import java.awt.Color;

public class Constants {
    // Panel constants.
    public static final int BLOCK_SIZE = 16;
    public static final int BLOCK_SCALE = 2;
    public static final int TILE_SIZE = BLOCK_SIZE * BLOCK_SCALE;
    public static final int PANEL_ROWS = 20;
    public static final int PANEL_COLS = 30;
    public static final int PANEL_WIDTH = PANEL_COLS * TILE_SIZE;
    public static final int PANEL_HEIGHT = PANEL_ROWS * TILE_SIZE;

    // Time constants.
    public static final int FRAME_RATE_SECONDS = 120;
    public static final long FRAME_TIME = 1_000_000_000;
    public static final long QUARTER_SECOND_MILLIS = 250;
    public static final long HALF_SECOND_MILLIS = 500;
    public static final long ONE_SECOND_MILLIS = 1_000;
    public static final long ONE_MILLI_NANO = 1_000_000;
    public static final long QUARTER_SECOND_NANO = QUARTER_SECOND_MILLIS * ONE_MILLI_NANO;
    public static final long HALF_SECOND_NANO = HALF_SECOND_MILLIS * ONE_MILLI_NANO;
    public static final long ONE_SECOND_NANO = ONE_SECOND_MILLIS * ONE_MILLI_NANO;
    public static final long DRAW_INTERVAL = Constants.FRAME_TIME / FRAME_RATE_SECONDS;
    public static final long BOMB_COOLDOWN = Constants.ONE_SECOND_NANO * 2;

    // Player constants.
    public static final int PLAYER_SPEED = 1;
    public static final int PLAYER_1_X = BLOCK_SIZE * BLOCK_SCALE * 2;
    public static final int PLAYER_2_X = PANEL_WIDTH - (BLOCK_SIZE * BLOCK_SCALE * 3);
    public static final int PLAYER_Y = PANEL_HEIGHT / 2;
    public static final int PLAYER_UI_X = TILE_SIZE;
    public static final int UI_Y = BLOCK_SIZE / 2;

    // Entity constants.
    public static final long BOMB_TIMER_NANO = ONE_SECOND_NANO * 4;
    public static final int MAX_PROPAGATIONS = 5;
    public static final int BLOCK_TYPE = 1;
    public static final int ENEMY_UI_X = PANEL_WIDTH - (BLOCK_SIZE * 7);

    // Color constants.
    public static final Color COLOR_GRAY_TRANSPARENT = new Color(128, 128, 128, 128);

    // JSON constants.
    public static final String ENTITY = "entity";
    public static final String ACTION = "action";

    public static final String BLOCK = "block";
    public static final String BOX = "box";
    public static final String BOMB = "bomb";
    public static final String EXPLOSION = "explosion";
    public static final String EXPLODED = "exploded";
    public static final String GAME_MAP = "gameMap";

    public static final String BOMB_PLACED = "bombPlaced";
    public static final String BOMB_EXPIRED = "bombExpired";
    public static final String DIRECTION = "direction";
    public static final String MOVE = "move";
    public static final String PLAYER_LOST = "playerLost";
    public static final String IMMUNITY_DISABLED = "immunityDisabled";
    public static final String PLAYER_HIT = "playerHit";

    public static final String UUID = "uuid";
    public static final String PLAYER_UUID = "playerUUID";
    public static final String BOMB_UUID = "bombUUID";
    public static final String EXPLOSION_UUID = "explosionUUID";
    public static final String EXPLODED_UUID = "explodedUUID";

    public static final String X = "x";
    public static final String Y = "y";

    public static final String ACK = "ack";
    public static final String START = "start";
}
