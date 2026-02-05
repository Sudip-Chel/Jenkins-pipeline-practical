package com.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.sound.sampled.*;
import java.io.InputStream;

public class CarRacingGame extends JPanel implements ActionListener, KeyListener {

    /* ================= GAME STATE ================= */
    private boolean startScreen = true;
    private boolean countingDown = false;
    private boolean finishing = false;
    private boolean gameOver = false;
    private boolean gameWon = false;

    /* ================= COUNTDOWN ================= */
    private int countdown = 3;

    /* ================= LANES ================= */
    private final int[] LANES = {170, 220, 270};

    /* ================= PLAYER ================= */
    private int playerLane = 1;
    private int carX = LANES[playerLane];
    private int carY = 420;

    private double speed = 0;
    private double maxSpeed = 6;
    private double acceleration = 0.05;
    private boolean boost = false;

    /* ================= AI CARS ================= */
    private int ai1Lane = 0, ai1Y = -200;
    private int ai2Lane = 2, ai2Y = -500;

    /* ================= OBSTACLE ================= */
    private int obstacleLane = 1;
    private int obstacleY = -300;

    /* ================= GAME LOGIC ================= */
    private int score = 0;
    private int level = 1;
    private int finishLineY = -3000;

    /* ================= SOUND ================= */
    private Clip engineClip;

    private Timer timer;
    private Random rand = new Random();

    public CarRacingGame() {
        setFocusable(true);
        addKeyListener(this);
        timer = new Timer(20, this);
        timer.start();
    }

    /* ================= DRAW ================= */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Sky
        g.setColor(new Color(135, 206, 235));
        g.fillRect(0, 0, 500, 200);

        // Grass
        g.setColor(new Color(34, 139, 34));
        g.fillRect(0, 200, 500, 400);

        // Road
        g.setColor(Color.DARK_GRAY);
        g.fillRect(150, 0, 200, 600);

        // Road markings
        g.setColor(Color.WHITE);
        for (int i = 0; i < 600; i += 40) {
            g.fillRect(245, i, 10, 20);
        }

        // Start Screen
        if (startScreen) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 32));
            g.drawString("CAR RACING GAME", 95, 260);
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.drawString("Press ENTER to Start", 150, 300);
            return;
        }

        // Countdown
        if (countingDown) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 70));
            g.drawString(countdown > 0 ? "" + countdown : "GO!", 200, 320);
            return;
        }

        // Finish Line
        g.setColor(Color.WHITE);
        g.fillRect(150, finishLineY, 200, 10);

        // Player car
        drawCar(g, carX, carY, Color.RED);
        if (boost) drawFlame(g);

        // AI cars
        drawCar(g, LANES[ai1Lane], ai1Y, Color.BLUE);
        drawCar(g, LANES[ai2Lane], ai2Y, Color.YELLOW);

        // Obstacle
        g.setColor(Color.BLACK);
        g.fillRect(LANES[obstacleLane], obstacleY, 40, 40);

        // HUD
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Score: " + score, 20, 30);
        g.drawString("Speed: " + String.format("%.1f", speed), 20, 50);
        g.drawString("Level: " + level, 20, 70);

        // Game Over
        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("GAME OVER", 140, 280);
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.drawString("Press R to Restart", 160, 310);
        }

        // Win
        if (gameWon) {
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("RACE FINISHED!", 115, 260);
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.drawString("Score: " + score, 190, 300);
            g.drawString("Level Reached: " + level, 160, 330);
            g.drawString("Press R to Race Again", 140, 360);
        }
    }

    /* ================= GAME LOOP ================= */
    @Override
    public void actionPerformed(ActionEvent e) {

        if (countingDown) {
            countdown--;
            if (countdown < 0) {
                countingDown = false;
                startEngineSound();
            }
            repaint();
            return;
        }

        if (!startScreen && !gameOver && !gameWon) {

            score++;
            finishLineY += speed;

            // Speed control
            if (!finishing && speed < maxSpeed) speed += acceleration;
            if (finishing && speed > 1) speed -= 0.05;

            // Level up
            if (score % 1000 == 0) {
                level++;
                maxSpeed += 0.5;
            }

            // Move AI & obstacle
            ai1Y += 3 + level;
            ai2Y += 2 + level;
            obstacleY += 4 + level;

            attemptLaneChange();

            if (ai1Y > 600) ai1Y = -200;
            if (ai2Y > 600) ai2Y = -500;
            if (obstacleY > 600) {
                obstacleY = -300;
                obstacleLane = rand.nextInt(3);
            }

            Rectangle player = new Rectangle(carX, carY, 40, 60);

            if (player.intersects(new Rectangle(LANES[ai1Lane], ai1Y, 40, 60)) ||
                player.intersects(new Rectangle(LANES[ai2Lane], ai2Y, 40, 60)) ||
                player.intersects(new Rectangle(LANES[obstacleLane], obstacleY, 40, 40))) {

                gameOver = true;
                stopEngineSound();
            }

            if (finishLineY >= carY + 40 && !finishing) finishing = true;
            if (finishing && speed <= 1) {
                gameWon = true;
                stopEngineSound();
            }
        }
        repaint();
    }

    /* ================= REALISTIC AI ================= */
    private void attemptLaneChange() {
        if (Math.abs(ai1Y - obstacleY) < 80 && ai1Lane == obstacleLane) {
            if (ai1Lane > 0) ai1Lane--;
            else ai1Lane++;
        }

        if (level >= 3 && Math.abs(ai2Y - obstacleY) < 80 && ai2Lane == obstacleLane) {
            if (ai2Lane < 2) ai2Lane++;
            else ai2Lane--;
        }
    }

    /* ================= INPUT ================= */
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (startScreen && key == KeyEvent.VK_ENTER) {
            startScreen = false;
            countingDown = true;
            countdown = 3;
        }

        if (key == KeyEvent.VK_LEFT && playerLane > 0) playerLane--;
        if (key == KeyEvent.VK_RIGHT && playerLane < 2) playerLane++;
        carX = LANES[playerLane];

        if (key == KeyEvent.VK_UP) {
            boost = true;
            speed += 0.2;
        }

        if (key == KeyEvent.VK_R && (gameOver || gameWon)) resetGame();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) boost = false;
    }

    @Override public void keyTyped(KeyEvent e) {}

    /* ================= DRAW HELPERS ================= */
    private void drawFlame(Graphics g) {
        g.setColor(Color.ORANGE);
        g.fillOval(carX + 14, carY + 62, 12, 16);
        g.setColor(Color.YELLOW);
        g.fillOval(carX + 17, carY + 66, 6, 10);
    }

    private void drawCar(Graphics g, int x, int y, Color body) {
        g.setColor(body);
        g.fillRoundRect(x, y, 40, 60, 12, 12);

        g.setColor(Color.CYAN);
        g.fillRect(x + 10, y + 14, 20, 10);

        g.setColor(Color.BLACK);
        g.fillOval(x - 4, y + 8, 8, 12);
        g.fillOval(x + 36, y + 8, 8, 12);
        g.fillOval(x - 4, y + 40, 8, 12);
        g.fillOval(x + 36, y + 40, 8, 12);
    }

    /* ================= SOUND ================= */
    private void startEngineSound() {
        try {
            InputStream audioSrc = getClass().getResourceAsStream("/engine.wav");
            AudioInputStream audio = AudioSystem.getAudioInputStream(audioSrc);
            engineClip = AudioSystem.getClip();
            engineClip.open(audio);
            engineClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception ignored) {}
    }

    private void stopEngineSound() {
        if (engineClip != null && engineClip.isRunning()) engineClip.stop();
    }

    /* ================= RESET ================= */
    private void resetGame() {
        playerLane = 1;
        carX = LANES[playerLane];
        speed = 0;
        maxSpeed = 6;
        score = 0;
        level = 1;
        finishLineY = -3000;
        ai1Y = -200;
        ai2Y = -500;
        obstacleY = -300;
        obstacleLane = rand.nextInt(3);
        gameOver = false;
        gameWon = false;
        finishing = false;
        countingDown = true;
        countdown = 3;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Car Racing Game");
        frame.setSize(500, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new CarRacingGame());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
