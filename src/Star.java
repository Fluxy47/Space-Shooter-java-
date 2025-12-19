package src;
import java.awt.*;

public class Star {
    int x, y, speed;

    public Star(int x, int y, int speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
    }

    public void move(int screenHeight) {
        y += speed;
        if (y > screenHeight) y = 0;
    }

    public void draw(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(x, y, 2, 2);
    }
}
