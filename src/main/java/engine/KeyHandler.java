package engine;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {
    boolean upPressed = false;
    boolean downPressed = false;
    boolean leftPressed = false;
    boolean rightPressed = false;
    boolean spacePressed = false;
    boolean spaceReleased = true;

    @Override
    public void keyTyped(KeyEvent keyEvent) {}

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();

        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
            upPressed = true;
        } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
            downPressed = true;
        } else if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
            leftPressed = true;
        } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
            rightPressed = true;
        } else if (spaceReleased && keyCode == KeyEvent.VK_SPACE) {
            spacePressed = true;
            spaceReleased = false;
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();

        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
            upPressed = false;
        } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
            downPressed = false;
        } else if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
            leftPressed = false;
        } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
            rightPressed = false;
        } else if (keyCode == KeyEvent.VK_SPACE) {
            spacePressed = false;
            spaceReleased = true;
        }
    }

    public void releaseKeys() {
        upPressed = false;
        downPressed = false;
        leftPressed = false;
        rightPressed = false;
        spacePressed = false;
        spaceReleased = true;
    }
}
