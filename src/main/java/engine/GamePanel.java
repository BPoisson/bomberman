package engine;

import game.Direction;
import game.client.GameClient;
import game.client.entities.Bomb;
import game.client.entities.Player;
import global.Constants;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class GamePanel extends JPanel implements Runnable {
    Thread gameThread;
    KeyHandler keyHandler;
    Player player;
    GameClient gameClient;
    List<String> messages;

    public GamePanel() {
        this.setPreferredSize(new Dimension(Constants.PANEL_WIDTH, Constants.PANEL_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyHandler);
        this.setFocusable(true);
        this.keyHandler = new KeyHandler();
        this.gameClient = new GameClient();
        this.player = new Player();
        this.messages = new LinkedList<>();
        addKeyListener(this.keyHandler);
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        long nextDrawTime = System.nanoTime() + Constants.DRAW_INTERVAL;

        while (gameThread != null) {
            long currTime = System.nanoTime();

            if (currTime < nextDrawTime) {
                continue;
            }
            update();
            repaint();
            nextDrawTime = currTime + Constants.DRAW_INTERVAL;
        }
    }

    public void update() {
        handleInput();
    }

    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D playerGraphics2D = (Graphics2D) graphics;
        playerGraphics2D.setColor(player.color);
        playerGraphics2D.fillRect(player.x, player.y, Constants.TILE_SIZE, Constants.TILE_SIZE);

        List<Graphics2D> bombGraphics2DList = new LinkedList<>();

        for (Bomb bomb : player.bombList) {
            Graphics2D bombGraphics2D = (Graphics2D) graphics;
            bombGraphics2D.setColor(bomb.color);
            bombGraphics2D.fillRect(bomb.x, bomb.y, Constants.TILE_SIZE, Constants.TILE_SIZE);
            bombGraphics2DList.add(bombGraphics2D);
        }
        playerGraphics2D.dispose();
        bombGraphics2DList.forEach(Graphics::dispose);
    }

    private void handleInput() {
        Direction dir = null;

        if (keyHandler.upPressed) {
            dir = Direction.UP;
            System.out.println(dir);
        } else if (keyHandler.downPressed) {
            dir = Direction.DOWN;
            System.out.println(dir);
        } else if (keyHandler.leftPressed) {
            dir = Direction.LEFT;
            System.out.println(dir);
        } else if (keyHandler.rightPressed) {
            dir = Direction.RIGHT;
            System.out.println(dir);
        }

        if (dir != null) {
            String response = this.gameClient.sendMessage(String.format("{\nuuid:%s,\naction:move,\ndir:%s\n}", player.uuid, dir));

            JSONObject responseObj = new JSONObject(response);
            player.x = responseObj.getInt("x");
            player.y = responseObj.getInt("y");
        }

        if (keyHandler.spacePressed) {
            this.messages.add(String.format("{\nuuid:%s,\naction:bomb\n}", player.uuid));
        }
    }
}