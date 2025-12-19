package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.io.File;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

    private Timer timer;
    private Player player;
    private ArrayList<Bullet> bullets;
    private ArrayList<Enemy> enemies;
    private ArrayList<Bullet> enemyBullets;
    private ArrayList<Star> stars;
    private Random random = new Random();

    private int score = 0;
    private int shootCooldown = 0;

    private boolean leftPressed, rightPressed, upPressed, downPressed, spacePressed;
    private boolean gameStarted = false;

    private GameState gameState = GameState.MENU;
    private JFrame frame;

    private int enemySpeed = 4;
    private final int maxEnemySpeed = 15;

    private int wave = 1;
    private int baseEnemies = 5;
    private int enemiesToSpawn;
    private int enemiesSpawned = 0;
    private int spawnCooldown = 0;
    private int spawnInterval = 50;

    private Font retroFont;
    private int waveTextTimer = 0;

    public GamePanel(JFrame frame) {
        this.frame = frame;
        setFocusable(true);
        addKeyListener(this);

        // Load retro font
        try {
            retroFont = Font.createFont(Font.TRUETYPE_FONT, new File("assets/retro.ttf")).deriveFont(50f);
        } catch(Exception e) {
            retroFont = new Font("Arial", Font.BOLD, 50);
        }

        // Initialize stars
        stars = new ArrayList<>();
        for(int i=0;i<100;i++){
            stars.add(new Star(random.nextInt(1200), random.nextInt(800), 1 + random.nextInt(3)));
        }

        timer = new Timer(16, this);
        timer.start();
    }

    private void startGame() {
        player = new Player(getWidth() / 2 - 20, getHeight() - 80);
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        enemyBullets = new ArrayList<>();
        score = 0;
        shootCooldown = 0;
        leftPressed = rightPressed = upPressed = downPressed = spacePressed = false;
        gameStarted = false;

        wave = 1;
        baseEnemies = 5;
        enemiesToSpawn = baseEnemies;
        enemiesSpawned = 0;
        spawnCooldown = 0;
        enemySpeed = 4;
        spawnInterval = 50;
        waveTextTimer = 100; // show first wave text

        gameState = GameState.PLAYING;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState == GameState.PLAYING) updateGame();
        repaint();
    }

    private void updateGame() {
        // Update stars
        for(Star s : stars) s.move(getHeight());

        if (!gameStarted) {
            if (spacePressed) gameStarted = true;
        }

        // Player movement
        if (leftPressed) player.moveLeft();
        if (rightPressed) player.moveRight(getWidth());
        if (upPressed) player.moveUp();
        if (downPressed) player.moveDown(getHeight());

        // Player shooting
        if (gameStarted && spacePressed && shootCooldown == 0) {
            bullets.add(new Bullet(player.getX() + 18, player.getY()));
            shootCooldown = 12; // bullet cooldown
        }
        if (shootCooldown > 0) shootCooldown--;

        // Update bullets
        bullets.removeIf(b -> { b.move(); return b.getY() < 0; });

        // Spawn enemies only when waveTextTimer finished
        if (waveTextTimer <= 0 && enemiesSpawned < enemiesToSpawn) {
            if (spawnCooldown <= 0) {
                int enemiesThisTick = 1;
                if (wave <= 2) enemiesThisTick = 1 + random.nextInt(2);
                else if (wave <= 4) enemiesThisTick = 2 + random.nextInt(2);
                else enemiesThisTick = 4 + random.nextInt(3);

                for (int i = 0; i < enemiesThisTick && enemiesSpawned < enemiesToSpawn; i++) {
                    int x = random.nextInt(getWidth() - 30);
                    enemies.add(new Enemy(x, 0, enemySpeed));
                    enemiesSpawned++;
                }

                spawnCooldown = spawnInterval;
            } else spawnCooldown--;
        } else if (waveTextTimer > 0) waveTextTimer--;

        // Update enemies
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy en = enemies.get(i);
            en.move();

            // Controlled enemy shooting
            if (en.canShoot()) {
                enemyBullets.add(new Bullet(en.getX() + 12, en.getY() + 30, true));
            }

            if (en.getY() > getHeight()) enemies.remove(i);
            else if (en.getBounds().intersects(player.getBounds())) {
                player.takeDamage(1);
                enemies.remove(i);
                if (player.getHealth() <= 0) gameState = GameState.GAME_OVER;
            }
        }

        // Update enemy bullets
        enemyBullets.removeIf(b -> {
            b.moveDown(getHeight());
            if (b.getBounds().intersects(player.getBounds())) {
                player.takeDamage(1);
                return true;
            }
            return b.getY() > getHeight();
        });

        // Player bullet collision with enemies
        for (int i = 0; i < bullets.size(); i++) {
            for (int j = 0; j < enemies.size(); j++) {
                if (bullets.get(i).getBounds().intersects(enemies.get(j).getBounds())) {
                    bullets.remove(i);
                    enemies.remove(j);
                    score += 10;
                    i--; break;
                }
            }
        }

        // Check wave complete
        if (enemiesSpawned >= enemiesToSpawn && enemies.isEmpty()) startNextWave();
    }

    private void startNextWave() {
        wave++;
        enemiesSpawned = 0;
        waveTextTimer = 120; // show wave text for 2 seconds

        if (wave <= 2) enemiesToSpawn = baseEnemies + wave * 4;
        else if (wave <= 4) enemiesToSpawn = baseEnemies + wave * 8;
        else if (wave <= 7) enemiesToSpawn = baseEnemies + wave * 12;
        else enemiesToSpawn = baseEnemies + wave * 18;

        enemySpeed++;
        if (enemySpeed > maxEnemySpeed) enemySpeed = maxEnemySpeed;

        if (wave >= 3) {
            spawnInterval -= 6;
            if (spawnInterval < 7) spawnInterval = 7;
        } else {
            spawnInterval -= 2;
            if (spawnInterval < 20) spawnInterval = 20;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Black background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw stars
        g.setColor(Color.WHITE);
        for(Star s : stars) g.fillRect(s.x, s.y, 1, 1);

        if (gameState == GameState.MENU) drawMenu(g);
        else if (gameState == GameState.PLAYING) drawGame(g);
        else if (gameState == GameState.GAME_OVER) drawGameOver(g);
    }

    private void drawMenu(Graphics g) {
        g.setColor(Color.CYAN);
        g.setFont(retroFont);
        g.drawString("SPACE SHOOTER", getWidth()/2 - 200, getHeight()/2 - 100);

        g.setFont(new Font("Arial", Font.PLAIN, 25));
        g.drawString("Press ENTER to Start", getWidth()/2 - 130, getHeight()/2);
        g.drawString("Press ESC to Exit", getWidth()/2 - 110, getHeight()/2 + 40);
    }

    private void drawGame(Graphics g) {
        // Player
        player.draw(g);

        // Bullets
        for (Bullet b : bullets) b.draw(g);
        for (Bullet b : enemyBullets) b.draw(g);

        // Enemies
        for (Enemy en : enemies) en.draw(g);

        // Score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("Score: " + score, 20, 30);

        // Health bar
        g.setColor(Color.RED);
        int barWidth = 100;
        int barHeight = 10;
        g.fillRect(20, 40, barWidth, barHeight);
        g.setColor(Color.GREEN);
        int healthWidth = (int)((player.getHealth() / (float)player.getMaxHealth()) * barWidth);
        g.fillRect(20, 40, healthWidth, barHeight);
        g.setColor(Color.WHITE);
        g.drawRect(20, 40, barWidth, barHeight);

        // Wave text
        if (waveTextTimer > 0) {
            g.setFont(retroFont.deriveFont(30f));
            g.setColor(Color.YELLOW);
            g.drawString("WAVE: " + wave, getWidth()/2 - 70, 70);
        }

        // Initial instructions
        if (!gameStarted) {
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.setColor(Color.CYAN);
            g.drawString("Press SPACE to shoot", getWidth()/2 - 100, getHeight()/2 + 50);
        }
    }

    private void drawGameOver(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(retroFont);
        g.drawString("GAME OVER", getWidth()/2 - 150, getHeight()/2 - 50);

        g.setFont(new Font("Arial", Font.PLAIN, 25));
        g.setColor(Color.WHITE);
        g.drawString("Final Score: " + score, getWidth()/2 - 90, getHeight()/2);
        g.drawString("Press ENTER to Restart", getWidth()/2 - 150, getHeight()/2 + 40);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (gameState == GameState.MENU) {
            if (key == KeyEvent.VK_ENTER) startGame();
            if (key == KeyEvent.VK_ESCAPE) exitFullscreen();
        }

        if (gameState == GameState.GAME_OVER) {
            if (key == KeyEvent.VK_ENTER) startGame();
            if (key == KeyEvent.VK_ESCAPE) exitFullscreen();
        }

        if (gameState == GameState.PLAYING) {
            if (key == KeyEvent.VK_LEFT) leftPressed = true;
            if (key == KeyEvent.VK_RIGHT) rightPressed = true;
            if (key == KeyEvent.VK_UP) upPressed = true;
            if (key == KeyEvent.VK_DOWN) downPressed = true;
            if (key == KeyEvent.VK_SPACE) spacePressed = true;
            if (key == KeyEvent.VK_ESCAPE) exitFullscreen();
        }
    }

    private void exitFullscreen() {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        gd.setFullScreenWindow(null);
        frame.dispose();
        frame.setUndecorated(false);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) leftPressed = false;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) rightPressed = false;
        if (e.getKeyCode() == KeyEvent.VK_UP) upPressed = false;
        if (e.getKeyCode() == KeyEvent.VK_DOWN) downPressed = false;
        if (e.getKeyCode() == KeyEvent.VK_SPACE) spacePressed = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
