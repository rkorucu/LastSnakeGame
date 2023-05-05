package com.example.lastsnake;

import java.util.*;
import javax.imageio.ImageIO;
import java.util.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

class Game extends JPanel {
    private Timer timer;
    private Snake snake;
    private Point cherry;
    private int points = 0;
    private int best = 0;
    private BufferedImage image;
    private GameStatus status;
    private boolean didLoadCherryImage = true;
    static final int UNIT_SIZE = 25;
    final int[] x = new int[GAME_UNITS];
    final int[] y = new int[GAME_UNITS];
    boolean running = false;
    int bodyParts = 6;
    int applesEaten;
    int appleX;
    int appleY;
    Random random;
    private static final Font FONT_S = new Font("MV Boli", Font.PLAIN, 20);
    private static final Font FONT_M = new Font("MV Boli", Font.PLAIN, 24);
    private static final Font FONT_M_ITALIC = new Font("MV Boli", Font.ITALIC, 24);
    private static final Font FONT_L = new Font("MV Boli", Font.PLAIN, 84);
    private static final Font FONT_XL = new Font("Monospaced", Font.PLAIN, 150);
    private static final int WIDTH = 760;
    private static final int HEIGHT = 520;
    private static final int DELAY = 50;
    static final int GAME_UNITS = (WIDTH * HEIGHT) / (UNIT_SIZE * UNIT_SIZE);

    // Constructor
    public Game() {

        random = new Random();
        this.addKeyListener(new KeyListener());
        this.setFocusable(true);
        this.setBackground(new Color(136, 199, 79));
        this.setDoubleBuffered(true);

        snake = new Snake(WIDTH / 2, HEIGHT / 2);
        status = GameStatus.NOT_STARTED;
        repaint();
        try {
            image = ImageIO.read(new File("com/example/lastsnake/cherry.png"));
        } catch (IOException e) {
            didLoadCherryImage = false;
            System.out.println("Aaa");
        }
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
        render(g);

        Toolkit.getDefaultToolkit().sync();
    }

    // Render the game
    private void update() {
        snake.move();

        if (cherry != null && snake.getHead().intersects(cherry, 20)) {
            snake.addTail();
            cherry = null;
            points++;
        }

        if (cherry == null) {
            spawnCherry();
        }

        checkForGameOver();
    }

    private void reset() {
        points = 0;
        cherry = null;
        snake = new Snake(WIDTH / 2, HEIGHT / 2);
        setStatus(GameStatus.RUNNING);
    }

    private void setStatus(GameStatus newStatus) {
        switch (newStatus) {
            case RUNNING:
                timer = new Timer();
                timer.schedule(new GameLoop(), 0, DELAY);
                break;
            case PAUSED:
                timer.cancel();
            case GAME_OVER:
                timer.cancel();
                best = points > best ? points : best;
                break;
        }

        status = newStatus;
    }

    private void togglePause() {
        setStatus(status == GameStatus.PAUSED ? GameStatus.RUNNING : GameStatus.PAUSED);
    }

    // Check if the snake has hit the wall or itself
    private void checkForGameOver() {
        Point head = snake.getHead();
        boolean hitBoundary = head.getX() <= 20
                || head.getX() >= WIDTH + 10
                || head.getY() <= 40
                || head.getY() >= HEIGHT + 30;

        boolean ateItself = false;

        for (Point t : snake.getTail()) {
            ateItself = ateItself || head.equals(t);
        }

        if (hitBoundary || ateItself) {
            setStatus(GameStatus.GAME_OVER);
        }
    }

    // Spawn a cherry at a random location
    public void drawCenteredString(Graphics g, String text, Font font, int y) {
        FontMetrics metrics = g.getFontMetrics(font);
        int x = (WIDTH - metrics.stringWidth(text)) / 2;

        g.setFont(font);
        g.drawString(text, x, y);
    }

    private void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);
        g2d.setFont(FONT_M);

        if (status == GameStatus.NOT_STARTED) {
            drawCenteredString(g2d, "SNAKE", FONT_XL, 200);
            drawCenteredString(g2d, "GAME", FONT_XL, 300);
            drawCenteredString(g2d, "Press  any  key  to  begin", FONT_M_ITALIC, 330);
            drawCenteredString(g2d, "You can use WASD or arrow keys to move", FONT_S, 380);
            drawCenteredString(g2d, "and pause/resume the game by pressing 'P'", FONT_S, 400);
            drawCenteredString(g2d, "by Ramazan Korucu", FONT_M_ITALIC, 500);
            return;
        }

        Point p = snake.getHead();

        g2d.drawString("SCORE: " + String.format("%02d", points), 20, 30);
        g2d.drawString("BEST: " + String.format("%02d", best), 630, 30);


        if (cherry != null) {
            if (didLoadCherryImage) {
                g2d.drawImage(image, cherry.getX(), cherry.getY(), 60, 60, null);
            } else {
                g2d.setColor(Color.RED);
                g2d.fillOval(cherry.getX(), cherry.getY(), 10, 10);
                g2d.setColor(Color.RED);
            }
        }

        if (status == GameStatus.GAME_OVER) {
            drawCenteredString(g2d, "GAME ", FONT_XL, 260);
            drawCenteredString(g2d, "OVER ", FONT_XL, 350);
            drawCenteredString(g2d, "Press any key to start again", FONT_M_ITALIC, 390);
        }

        if (status == GameStatus.PAUSED) {
            g2d.drawString("Paused", 600, 14);
        }

//        g2d.setColor(new Color(33, 70, 199));
        g2d.fillRect(p.getX(), p.getY(), 10, 10);




        for (int i = 0, size = snake.getTail().size(); i < size; i++) {
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            Point t = snake.getTail().get(i);

            g2d.fillRect(t.getX(), t.getY(), 10, 10);
        }

        g2d.setColor(new Color(32, 94, 17));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(20, 40, WIDTH, HEIGHT);
    }

    // spawn cherry in random position
    public void spawnCherry() {
        cherry = new Point((new Random()).nextInt(WIDTH - 60) + 20,
                (new Random()).nextInt(HEIGHT - 60) + 40);
    }


    private class KeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (status == GameStatus.RUNNING) {
                if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
                    snake.turn(Direction.LEFT);
                } else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
                    snake.turn(Direction.RIGHT);
                } else if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
                    snake.turn(Direction.UP);
                } else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
                    snake.turn(Direction.DOWN);
                }
            }

            if (status == GameStatus.NOT_STARTED) {
                setStatus(GameStatus.RUNNING);
            }

            if (status == GameStatus.GAME_OVER) {
                reset();
            }

            if (key == KeyEvent.VK_P) {
                togglePause();
            }
        }
    }

    private class GameLoop extends java.util.TimerTask {
        public void run() {
            update();
            repaint();
        }
    }

    public void draw(Graphics g) {

        if (running) {
			/*
			for(int i=0;i<SCREEN_HEIGHT/UNIT_SIZE;i++) {
				g.drawLine(i*UNIT_SIZE, 0, i*UNIT_SIZE, SCREEN_HEIGHT);
				g.drawLine(0, i*UNIT_SIZE, SCREEN_WIDTH, i*UNIT_SIZE);
			}
			*/
            g.setColor(Color.red);
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g.setColor(Color.green);
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                } else {
                    g.setColor(new Color(45, 180, 0));
                    g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                }
            }
            g.setColor(Color.red);
            g.setFont(new Font("Ink Free", Font.BOLD, 40));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: " + applesEaten, (WIDTH - metrics.stringWidth("Score: " + applesEaten)) / 2, g.getFont().getSize());
        }

    }

}
