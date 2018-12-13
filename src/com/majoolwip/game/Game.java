package com.majoolwip.game;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class Game {

  private final double FRAME_CAP = 1.0 / 60.0;
  private final int WIDTH = 320;
  private final int HEIGHT = 160;
  private final float SCALE = 4.0f;
  private final float FOV = (float) Math.toRadians(75);
  private final String TITLE = "Wolfenstein By Ryan Moore";
  private final float STEP_SIZE = 0.001f;

  private Frame frame;
  private BufferedImage image;
  private Input input;
  private int[] pixels;

  private boolean running = false;

  // Map Variables ///
  int mapWidth = 10;
  int[] map = {
      1,1,1,1,1,1,1,1,1,1,
      1,0,0,0,0,0,0,0,0,1,
      1,0,0,0,0,1,1,0,0,1,
      1,0,0,0,0,1,1,0,0,1,
      1,0,0,0,0,0,0,0,0,1,
      1,1,1,1,1,1,1,1,1,1
  };

  // Player Variables ///
  float pX, pY, angle;

  private Game() throws InterruptedException {
    pX = 3.0f;
    pY = 3.0f;
    angle = 0.0f;

    initializeWindow();
    gameLoop();
  }

  private void initializeWindow() {
    image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
    frame = new Frame(TITLE);
    frame.addWindowListener(new WinListener());
    input = new Input();
    frame.addKeyListener(input);
    frame.setResizable(false);
    frame.setVisible(true);
  }

  private void gameLoop() throws InterruptedException {
    running = true;
    double firstTime = 0.0;
    double lastTime = System.nanoTime() / 1000000000.0;
    double passedTime = 0.0;
    double unprocessedTime = 0.0;
    while(running) {
      boolean render = false;
      firstTime = System.nanoTime() / 1000000000.0;
      passedTime = firstTime - lastTime;
      lastTime = firstTime;
      unprocessedTime += passedTime;
      while (unprocessedTime >= FRAME_CAP) {
        unprocessedTime -= FRAME_CAP;
        update((float) FRAME_CAP);
        input.update();
        render = true;
      }
      if (render) {
        clear();
        render();
        updateScreen();

      } else {
        Thread.sleep(1);
      }
    }
    dispose();
  }

  private void update(float dt) {
    if (input.isKey(KeyEvent.VK_LEFT)) {
      angle -= dt;
    }
    if (input.isKey(KeyEvent.VK_RIGHT)) {
      angle += dt;
    }
    if (input.isKey(KeyEvent.VK_UP)) {
      pX += Math.cos(angle) * dt;
      pY += Math.sin(angle) * dt;
    }
  }

  private void render() {
    checkScreenSize();
    for(int i = 0; i < WIDTH; i++) {
      float distance = 0;
      float rayX = pX, rayY = pY;
      while (map[(int)rayX + (int)rayY * mapWidth] == 0) {
        float dx = (float) Math.cos(angle - (FOV / 2) + FOV * (i / (float)WIDTH));
        float dy = (float) Math.sin(angle - (FOV / 2) + FOV * (i / (float)WIDTH));
        rayX += dx * STEP_SIZE;
        rayY += dy * STEP_SIZE;
      }
      distance = (float) Math.sqrt(Math.pow(rayX - pX, 2.0) + Math.pow(rayY - pY, 2.0));
      int top = (int)(HEIGHT / (distance * 2.0));
      for (int j = HEIGHT / 2 - top; j < HEIGHT / 2 + top; j++) {
        if(j < 0 || j >= HEIGHT) {
          continue;
        }
        pixels[i + j * WIDTH] = 0xffffffff;
      }
    }
  }

  private void updateScreen() {
    BufferStrategy bs = frame.getBufferStrategy();
    if(bs == null) {
      frame.createBufferStrategy(2);
      bs = frame.getBufferStrategy();
    }
    Graphics g = bs.getDrawGraphics();
    Insets in = frame.getInsets();
    g.drawImage(image, in.left, in.top, (int)(WIDTH * SCALE), (int)(HEIGHT * SCALE), null);
    bs.show();
    g.dispose();
  }

  private void clear() {
    for(int i = 0; i < pixels.length; i++) {
      pixels[i] = 0xff404040;
    }
  }

  private void checkScreenSize() {
    Insets in = frame.getInsets();
    if (frame.getWidth() != in.left + (int)(WIDTH * SCALE) + in.right) {
      frame.setSize(in.left + (int)(WIDTH * SCALE) + in.right, frame.getHeight());
    }
    if (frame.getHeight() != in.top + (int)(HEIGHT * SCALE) + in.bottom) {
      frame.setSize(frame.getWidth(),in.top + (int)(HEIGHT * SCALE) + in.bottom);
    }
  }

  private void dispose() {
    frame.dispose();
  }

  public static void main(String args[]) throws InterruptedException {
    new Game();
  }

  private class Input implements KeyListener {

    private boolean[] keys = new boolean[256];
    private boolean[] keysLast = new boolean[256];

    public void update() {
      System.arraycopy(keys, 0, keysLast, 0, 256);
    }

    public boolean isKey(int keyCode) {
      return keys[keyCode];
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
      keys[e.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
      keys[e.getKeyCode()] = false;
    }
  }

  private class WinListener implements WindowListener {
    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
      running = false;
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
  }
}
