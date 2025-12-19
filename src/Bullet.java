package src;
import javax.swing.*;
import java.awt.*;

public class Bullet {
    private int x, y;
    private final int width = 8;
    private final int height = 16;
    private final int speed = 8;
    private Image img;
    private boolean isEnemy;

    // Player bullet constructor
    public Bullet(int x, int y) {
        this(x, y, false);
    }

    // General constructor
    public Bullet(int x, int y, boolean isEnemy) {
        this.x = x;
        this.y = y;
        this.isEnemy = isEnemy;
        try {
            img = new ImageIcon("assets/bullet.png").getImage();
        } catch (Exception e) {
            img = null; // fallback to rectangle
        }
    }

    // Move
    public void move() {
        if (!isEnemy) y -= speed;
        else y += speed; // enemy bullets go down
    }

    public void moveDown(int screenHeight) {
        y += speed;
    }

    public void draw(Graphics g) {
        if (img != null) g.drawImage(img, x, y, width, height, null);
        else {
            g.setColor(isEnemy ? Color.RED : Color.YELLOW);
            g.fillRect(x, y, width, height);
        }
    }

    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    public int getY() { return y; }
}
