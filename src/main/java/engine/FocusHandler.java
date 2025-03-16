package engine;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class FocusHandler implements FocusListener {
    KeyHandler keyHandler;

    public FocusHandler(KeyHandler keyHandler) {
        this.keyHandler = keyHandler;
    }

    @Override
    public void focusGained(FocusEvent e) {}

    @Override
    public void focusLost(FocusEvent e) {
        this.keyHandler.releaseKeys();
    }
}
