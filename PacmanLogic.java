import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class PacmanLogic {
    private static volatile boolean IS_RUNNING = false;
    
    private static int FPS = 60;
    private static long TIME_PER_FRAME = 1000L / FPS;
    private static int frameCounter = 0;

    private static JPanel MAP_PANEL;
    private static int[][] MAP = generateMap();
    private static int xBound = MAP[0].length;
    private static int yBound = MAP.length;

    private static volatile int CURRENT_X_LOCATION = 1;
    private static volatile int CURRENT_Y_LOCATION = 1;
    private static Direction currentDirection = Direction.LEFT;
    private static Direction desiredDirection = Direction.LEFT;
    
    private static boolean mouthOpen = false;
    private static int speed = 10; // Higher is slower.
    
    private enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    public static void main(String[] args) {

        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());

        JButton startButton = new JButton();
        startButton.setText("Click to start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!IS_RUNNING) {
                    IS_RUNNING = true;
                    System.out.println("Game started.");
                    startGameLoop();
                } else {
                    System.out.println("Game is ongoing.");
                }
            }
        });

        JButton stopButton = new JButton();
        stopButton.setText("Click to stop");
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (IS_RUNNING) {
                    IS_RUNNING = false;
                    System.out.println("Game stopped.");
                } else {
                    System.out.println("Game hasn't started.");
                }
            }
        });
        
        startButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP -> desiredDirection = Direction.UP;
                    case KeyEvent.VK_DOWN -> desiredDirection = Direction.DOWN;
                    case KeyEvent.VK_RIGHT -> desiredDirection = Direction.RIGHT;
                    case KeyEvent.VK_LEFT -> desiredDirection = Direction.LEFT;
                }
            }
        });

        MapPanel MapPanel = new MapPanel();
        MAP_PANEL = MapPanel;

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 2));
        panel.add(startButton);
        panel.add(stopButton);
        // frame.add(startButton, BorderLayout.LINE_START);
        // frame.add(stopButton, BorderLayout.LINE_END);
        frame.add(panel, BorderLayout.NORTH);
        frame.add(MapPanel, BorderLayout.CENTER);
        
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static void startGameLoop() {
        new Thread(() -> {
            while (IS_RUNNING) {
                long start = System.currentTimeMillis();

                update();
                render();

                long end = System.currentTimeMillis();
                long elapsed = end - start;

                // Calculate remaining frames left. Positvie means game ran too fast
                // and can sleep to maintain desired FPS. Negative == FPS drop. 
                long wait = TIME_PER_FRAME - elapsed;

                if (wait > 0) {
                    try {
                        Thread.sleep(wait); // Sleep for the remaining of the TIME_PER_FRAMEms.
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            System.out.println("GameLoop: Interrupted.");
            
        }).start();
    }

    private static void update() {
        frameCounter++;
        if (frameCounter % 10 == 0) {
            mouthOpen = !mouthOpen;
        }

        if (frameCounter % speed == 0) {
            move(); // actually move Pac-Man
        }
    };

    private static void move() {
        if (isMoveValid(desiredDirection)) {
            currentDirection = desiredDirection;
        } else if (!isMoveValid(currentDirection)) {
            return;
        }

        // Updates the current (x,y) coordinate.
        CURRENT_X_LOCATION = 
                (CURRENT_X_LOCATION + getDeltaX(currentDirection) + xBound) % xBound;
        CURRENT_Y_LOCATION = 
                (CURRENT_Y_LOCATION + getDeltaY(currentDirection) + yBound) % yBound;
    }

    // Determines if the current (x,y) is valid if moved from the given direction.
    private static boolean isMoveValid(Direction direction) {
        int newX = (CURRENT_X_LOCATION + getDeltaX(direction) + xBound) % xBound;
        int newY = (CURRENT_Y_LOCATION + getDeltaY(direction) + yBound) % yBound;

        return (newX >= 0 && newX < xBound) // Within the bounds of the map.
            && (newY >= 0 && newY < yBound)
               && MAP[newY][newX] != 1; // Not a wall.
    }
    
    // Increment x-coordinate based on the direction. Allows wrapping to 
    // opposite side if possible.
    private static int getDeltaX(Direction direction) {
        switch (direction) {
            case Direction.LEFT -> {
                return -1;
            }
            case Direction.RIGHT -> {
                return +1;
            }
            default -> {
                return 0;
            }
        }
    }
    
    // Increment y-coordinate based on the direction. Allows wrapping to 
    // opposite side if possible.
    private static int getDeltaY(Direction direction) {
        switch (direction) {
            case Direction.UP -> {
                return -1;
            }
            case Direction.DOWN -> {
                return +1;
            }
            default -> {
                return 0;
            }
        }
    }

    // Paints the current state of the array map environment on the MAP_PANEL.
    private static void render() {
        MAP_PANEL.repaint();
    };

    // Returns a double array representing the game environment.
    // 1-Wall, 0-Corridor
    // Could possibly add 2 as a pellet, or maybe create a whole new array for that? idk yet.
    private static int[][] generateMap() {
        return new int[][] {
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 0, 1},
            {1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 0, 1},
            {1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1},
            {1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 0, 1},
            {1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 0, 1},
            {1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 1},
            {1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
            {1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1}
        };
    }

    // Custom JPanel that paints the current state of array map environment.
    static class MapPanel extends JPanel {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            int rows = MAP.length;
            int columns = MAP[0].length;

            // Width/Height of one block corresponding to one element in the
            // map array. Varies depending on the size of the JPanel
            int blockWidth = getWidth() / columns;
            int blockHeight = getHeight() / rows;

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < columns; col++) {
                    // Pixel location of top-left corner of the block
                    int x = col * blockWidth;
                    int y = row * blockHeight;

                    // For corridor or wall.
                    if (MAP[row][col] == 1) {
                        g.setColor(Color.BLUE);
                    } else {
                        g.setColor(Color.BLACK);
                    }

                    g.fillRect(x, y, blockWidth, blockHeight);

                    // Pacman is in the current (x,y) coordinate.
                    if (row == CURRENT_Y_LOCATION && col == CURRENT_X_LOCATION) {
                        g.setColor(Color.yellow);

                        if (mouthOpen) {
                            // Draw full circle (mouth closed)
                            g.fillOval(x, y, blockWidth, blockHeight);
                        } else {
                            // Draw "open mouth" as a wedge
                            int startAngle = 30;
                            int arcAngle = 300;

                            switch (currentDirection) {
                                case Direction.RIGHT -> startAngle = 30;
                                case Direction.LEFT  -> startAngle = 210;
                                case Direction.UP    -> startAngle = 120;
                                case Direction.DOWN  -> startAngle = 300;
                            }
                            
                            g.fillArc(x, y, blockWidth, blockHeight, startAngle, arcAngle);
                        }
                    }
                }
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(700, 700);
        }
    }
}


