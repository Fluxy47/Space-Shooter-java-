package src;
import java.awt.*;
import javax.swing.*;

public class Player {
    private int x, y;
    private final int width = 40;
    private final int height = 40;
    private final int speed = 12;
    private Image img;

    private int health = 5;
    private int maxHealth = 5;
    private int damageTimer = 0; // for flash effect

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        img = new ImageIcon("assets/player.png").getImage();
    }

    public void moveLeft() { x -= speed; if (x < 0) x = 0; }
    public void moveRight(int screenWidth) { x += speed; if (x > screenWidth - width) x = screenWidth - width; }
    public void moveUp() { y -= speed; if (y < 0) y = 0; }
    public void moveDown(int screenHeight) { y += speed; if (y > screenHeight - height) y = screenHeight - height; }

    public void draw(Graphics g) {
        if (damageTimer > 0) {
            g.setColor(Color.RED);
            g.fillRect(x, y, width, height); // flash red
            damageTimer--;
        } else {
            g.drawImage(img, x, y, width, height, null);
        }
    }

    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }

    public int getX() { return x; }
    public int getY() { return y; }

    public void takeDamage(int dmg) {
        health -= dmg;
        if (health < 0) health = 0;
        damageTimer = 10; // flash for 10 ticks
    }

    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
}
