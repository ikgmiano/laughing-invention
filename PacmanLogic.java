import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class PacmanLogic {
    private static int FPS = 60;
    private static long TIME_PER_FRAME = 1000L / FPS;

    private static volatile boolean IS_RUNNING = false;

    private static volatile int[][] MAP = generateMap(30);
    private static JLabel MAP_LABEL;

    private static double moveSpeed = 10; // tiles per second, can be 1, 2, or 3
    private static double moveAccumulator = 0; // tracks time since last tile move

    private static volatile int CURRENT_X_LOCATION = 1;
    private static volatile int CURRENT_Y_LOCATION = 1;
    private static Direction currentDirection = Direction.LEFT;
    private static Direction desiredDirection = Direction.LEFT;

    private enum Character {
        WALL("<span style='color:blue;'>" + '\u2588' + "</span>"),
        EMPTY("<span style='color:white;'>" + '\u2588' + "</span>"),
        // PLAYER('\u25CF');
        PLAYER("<span style='color:yellow;'>" + '\u2588' + "</span>");

        private final String value;

        Character(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }        
    }

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

        JLabel map = new JLabel();
        map.setFont(new Font("Monospaced", Font.PLAIN, 16));
        map.setHorizontalAlignment(SwingConstants.LEFT);
        map.setVerticalAlignment(SwingConstants.TOP);
        MAP_LABEL = map;

        frame.add(startButton, BorderLayout.LINE_START);
        frame.add(stopButton, BorderLayout.LINE_END);
        frame.add(map, BorderLayout.CENTER);
        
        frame.setSize(1000, 1000);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        frame.requestFocus();
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
        // move();

        double deltaTime = 1.0 / FPS; // 60 FPS → ~0.0167s per frame
        moveAccumulator += deltaTime;

        double timePerTile = 1.0 / moveSpeed; // seconds per tile
        if (moveAccumulator >= timePerTile) {
            move(); // actually move Pac-Man
            moveAccumulator -= timePerTile;
        }
    };

    private static void move() {
        // Check if buffered next move is allowed
        // check for bounds and check for availability of movement
        // update current location

        /*
            if can move desiredDirection
                currentDirection = desiredDirection

            actual movement update
        */

        if (canChangeDirection()) {
            currentDirection = desiredDirection;
        }
        
        int newX = CURRENT_X_LOCATION;
        int newY = CURRENT_Y_LOCATION;

        int xLength = MAP[0].length;
        int yLength = MAP.length;

        // Increment xy coordinate based on the current movement direction.
        switch (currentDirection) {
            case Direction.UP -> newY = (newY - 1 + yLength) % yLength;
            case Direction.DOWN -> newY = (newY + 1) % yLength;
            case Direction.RIGHT -> newX = (newX + 1) % xLength;
            case Direction.LEFT -> newX = (newX - 1 + xLength) % xLength;
        }


        if (isMoveValid(newX, newY)) {
            CURRENT_X_LOCATION = newX;
            CURRENT_Y_LOCATION = newY;
        }
    }

    private static boolean canChangeDirection() {
        int newX = CURRENT_X_LOCATION;
        int newY = CURRENT_Y_LOCATION;

        int xLength = MAP[0].length;
        int yLength = MAP.length;

        switch (desiredDirection) {
            case Direction.UP -> newY = (newY - 1 + yLength) % yLength;
            case Direction.DOWN -> newY = (newY + 1) % yLength;
            case Direction.RIGHT -> newX = (newX + 1) % xLength;
            case Direction.LEFT -> newX = (newX - 1 + xLength) % xLength;
        }

        return isMoveValid(newX, newY);
    }

    // Determines if the given (x, y) coordinate is valid, that means it's within the
    // map's bounds and is not a wall.
    private static boolean isMoveValid(int x, int y) {
        return (x >= 0 && x < MAP[0].length) // Within the bounds of the map.
            && (y >= 0 && y < MAP.length)
               && MAP[y][x] != 1; // Not a wall.
    }

    private static void render() {
        StringBuilder s = new StringBuilder();

        s.append("<html><pre style='font-family:monospace;'>");
        for (int i = 0; i < MAP.length; i++) {
            for (int j = 0; j < MAP[i].length; j++) {;
                if (i == CURRENT_Y_LOCATION && j == CURRENT_X_LOCATION) {
                    s.append(Character.PLAYER.getValue());
                } else {
                    s.append(MAP[i][j] == 0 ? Character.EMPTY.getValue() : Character.WALL.getValue());
                }
                // s += "  ";
            }
            s.append("\n");
        }
        s.append("</pre></html>");

        MAP_LABEL.setText(s.toString());
    };

    private static int[][] generateMap(int dimension) {
        // char[][] map = new char[dimension][dimension];
        // Random random = new Random();
        // for (int i = 0; i < map.length; i++) {
        //     for (int j = 0; j < map[i].length; j++) {
        //         if (random.nextBoolean()) {
        //             map[i][j] = '-';
        //         } else {
        //             map[i][j] = '#';
        //         }
        //     }
        // }
        int[][] map = {
            //   0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24
            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1 }, // 0
            { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 }, // 1
            { 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1 }, // 2
            { 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1 }, // 3
            { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 }, // 4
            { 1, 0, 1, 1, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1, 1, 1, 0, 1 }, // 5
            { 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1 }, // 6
            { 1, 1, 1, 0, 1, 0, 1, 1, 1, 1, 1, 0, 1, 0, 1, 1, 1, 1, 1, 0, 1, 0, 1, 1, 1 }, // 7
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, // 8 (This row looks like a path/warp tunnel)
            { 1, 1, 1, 0, 1, 0, 1, 1, 1, 1, 1, 0, 1, 0, 1, 1, 1, 1, 1, 0, 1, 0, 1, 1, 1 }, // 9
            { 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1 }, // 10
            { 1, 0, 1, 1, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1, 1, 1, 0, 1 }, // 11
            { 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1 }, // 12 (Center Row - Path on left/right, Ghost House in middle)
            { 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1 }, // 13 (Ghost House Top)
            { 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1 }, // 14 (Ghost House Sides)
            { 1, 0, 1, 0, 1, 1, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 1, 1, 0, 1, 0, 1, 0, 1 }, // 15
            { 1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 0, 1, 0, 0, 0, 1, 0, 1, 0, 1 }, // 16
            { 1, 0, 0, 0, 1, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1, 0, 0, 0, 1 }, // 17
            { 1, 1, 1, 0, 1, 0, 1, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 1, 0, 1, 1, 1 }, // 18
            { 1, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 0, 0, 0, 0, 1 }, // 19
            { 1, 0, 1, 1, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 0, 1 }, // 20
            { 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1 }, // 21
            { 1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1 }, // 22
            { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 }, // 23
            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1 }  // 24
        };






        return map;
    }
}
