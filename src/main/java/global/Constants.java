package global;

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
    public static final int FRAME_RATE_SECONDS = 60;
    public static final long FRAME_TIME = 1_000_000_000;
    public static final long ONE_SECOND_MILLIS = 1_000;
    public static final long ONE_MILLI_NANO = 1_000_000;
    public static final long ONE_SECOND_NANO = ONE_SECOND_MILLIS * ONE_MILLI_NANO;
    public static final long DRAW_INTERVAL = Constants.FRAME_TIME / FRAME_RATE_SECONDS;

    // Player constants.
    public static final int PLAYER_SPEED = 3;

    // Entity constants.
    public static final long BOMB_TIMER_NANO = ONE_SECOND_NANO * 4;
}
