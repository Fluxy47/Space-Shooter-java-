package src;

import java.awt.*;
import javax.swing.*;
import java.util.Random;

public class Enemy {
    private int x, y;
    private int speed;
    private final int width = 30;
    private final int height = 30;
    private Image img;

    private int shootCooldown;
    private final int maxShootCooldown = 90; // frames (~1.5s at 60 FPS)
    private Random random = new Random();

    public Enemy(int x, int y, int speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        img = new ImageIcon("assets/enemy.png").getImage(); // ensure enemy.png exists

        // Random initial cooldown so enemies don't all shoot simultaneously
        this.shootCooldown = random.nextInt(maxShootCooldown);
    }

    public void move() { y += speed; }

    public void draw(Graphics g) {
        g.drawImage(img, x, y, width, height, null);
    }

    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }

    public int getX() { return x; }

    public int getY() { return y; }

    // Controlled shooting
    public boolean canShoot() {
        if (shootCooldown <= 0) {
            shootCooldown = maxShootCooldown;
            return true;
        } else {
            shootCooldown--;
            return false;
        }
    }
}
