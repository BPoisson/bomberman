package engine;

import game.Direction;
import game.constants.Constants;
import game.entities.Bomb;
import game.entities.Player;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class GamePanel extends JPanel implements Runnable {
    long prevTime = 0;
    Thread gameThread;
    KeyHandler keyHandler = new KeyHandler();
    Player player = new Player();

    public GamePanel() {
        this.setPreferredSize(new Dimension(Constants.PANEL_WIDTH, Constants.PANEL_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyHandler);
        this.setFocusable(true);
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
        player.expireBombs();
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
        if (keyHandler.upPressed) {
            player.y -= player.speed;
            player.direction = Direction.UP;
        } else if (keyHandler.downPressed) {
            player.y += player.speed;
            player.direction = Direction.DOWN;
        } else if (keyHandler.leftPressed) {
            player.x -= player.speed;
            player.direction = Direction.LEFT;
        } else if (keyHandler.rightPressed) {
            player.x += player.speed;
            player.direction = Direction.RIGHT;
        }
        if (keyHandler.spacePressed && player.canPlaceBomb) {
            switch (player.direction) {
                case Direction.UP: {
                    Bomb bomb = new Bomb(player.x, player.y - Constants.TILE_SIZE);
                    player.bombList.add(bomb);
                    break;
                }
                case Direction.DOWN: {
                    Bomb bomb = new Bomb(player.x, player.y + Constants.TILE_SIZE);
                    player.bombList.add(bomb);
                    break;
                }
                case Direction.LEFT: {
                    Bomb bomb = new Bomb(player.x - Constants.TILE_SIZE, player.y);
                    player.bombList.add(bomb);
                    break;
                }
                case Direction.RIGHT: {
                    Bomb bomb = new Bomb(player.x + Constants.TILE_SIZE, player.y);
                    player.bombList.add(bomb);
                    break;
                }
                default:
            }
            player.canPlaceBomb = false;
        }
        if (!keyHandler.spacePressed) {
            player.canPlaceBomb = true;
        }
    }
}