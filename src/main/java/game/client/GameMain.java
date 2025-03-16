package game.client;

import engine.GamePanel;

import javax.swing.*;

public class GameMain {

    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Bomberman");

        GamePanel panel = new GamePanel();
        window.add(panel);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        panel.startGameThread();
    }
}
