import java.util.Scanner;
import java.util.Arrays;
import java.util.Random;
import java.lang.Thread;

public class SudokuTagasagot {
    static Scanner scanner = new Scanner(System.in);
    static Random random = new Random();
    static Thread thread = new Thread();

    static int[][] puzzle;
    static int stepCount;
    static boolean animated = true;
    public static void main(String[] args) {
        // System.out.print("0-Generate, 1-Enter: ");
        // puzzle = (scanner.nextInt() == 0) ? randomGeneration() : manualGeneration();
        
        // int[][] solution = copyGrid(puzzle);
        // solve(solution);

        // displaySudokuGrid(puzzle);

        // System.out.println();
        // System.out.println();

        // displaySudokuGrid(solution);
    }

    // check if entry already exists in the row
    private static boolean isEntryInRow(int[][] sudokuGrid, int row, int column, int entry) {
        for (int i = 0; i < sudokuGrid[row].length; i++) {
            if (i != column && sudokuGrid[row][i] == entry) { // skip the column where entry is
                return true;
            }
        }
        return false;
    }
    
    // check if entry already exists in the column
    private static boolean isEntryInColumn(int[][] sudokuGrid, int row, int column, int entry) {
        for (int i = 0; i < sudokuGrid.length; i++) {
            if (i != row && sudokuGrid[i][column] == entry) { // skip the row where entry is
                return true;
            }
        }
        return false;
    }
    
    // check if entry already exists in the corresponding 3x3 grid
    private static boolean isEntryInGrid(int[][] sudokuGrid, int row, int column, int entry) {
        int rowStart;
        int rowEnd;
        int columnStart;
        int columnEnd;

        // setting row 3x3 boundaries (inclusive)
        if (row < 3) {
            rowStart = 0;
            rowEnd = 2;
        } else if (row < 6) {
            rowStart = 3;
            rowEnd = 5;
        } else {
            rowStart = 6;
            rowEnd = 8;
        }
        
        // setting column 3x3 boundaries (inclusive)
        if (column < 3) {
            columnStart = 0;
            columnEnd = 2;
        } else if (column < 6) {
            columnStart = 3;
            columnEnd = 5;
        } else {
            columnStart = 6;
            columnEnd = 8;
        }

        for (int i = rowStart; i <= rowEnd; i++) {
            for (int j = columnStart; j <= columnEnd; j++) {
                // skip the [i][j] where entry is
                if ((i != row || j != column) && sudokuGrid[i][j] == entry) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isValid(int[][] sudokuGrid, int row, int column, int entry) {
        return !isEntryInRow(sudokuGrid, row, column, entry) &&
               !isEntryInColumn(sudokuGrid, row, column, entry) &&
               !isEntryInGrid(sudokuGrid, row, column, entry);
    }

    // sudoku solver
    private static void solve(int[][] sudokuGrid) {
        solve(sudokuGrid, 0, 0);
    }

    private static boolean solve(int[][] sudokuGrid, int row, int column) {
        // recursive check (if rod is beyond row 8, it's already solved)
        if (row == 9) {
            return true;
        }

        int currentRow = -1;
        int currentColumn = -1;
        OUTER:
        for (int i = row; i < sudokuGrid.length; i++) {
            // If i is given row, start at given column. Otherwise start from 0 for new rows
            // Basically start from given row and column at the beginning
            int j = (i == row) ? column : 0;
            for (; j < sudokuGrid[i].length; j++) {
                if (sudokuGrid[i][j] == 0) {
                    currentRow = i;
                    currentColumn = j;
                    break OUTER;
                }
            }
        }

        // Did not find any zero (unfilled) slots
        if (currentRow == -1) {
            return true;
        }

        // Try numbers 1 to 9 using backtracking for dead-ends
        for (int i = 1; i <= 9; i++) {
            if (isValid(sudokuGrid, currentRow, currentColumn, i)) { 
                sudokuGrid[currentRow][currentColumn] = i;
                stepCount++;

                int nextRow = (currentColumn == 8) ? currentRow + 1 : currentRow;
                int nextCol = (currentColumn == 8) ? 0 : currentColumn + 1;

                // Backtracking. This would return false if the current stack 
                // frame failed 1 to 9 entries, proceeding to the next iteration
                if (solve(sudokuGrid, nextRow, nextCol)) {
                    return true;
                }
            }
        }

        // If the for-loop failed all 1 to 9 entries, return false to do another 
        // iteration on the previous stack call's for-loop
        sudokuGrid[currentRow][currentColumn] = 0;
        stepCount++;
        return false;
    }

    // HELPER METHODS
    public static int[][] randomGeneration(int[][] matrix) {
        // int[][] matrix = new int[9][9];
        int i = 1;
        int j;
        while (i <= 9) {
            while (true) {
                j = random.nextInt(9);
                if (matrix[0][j] == 0) {
                    break;
                }
            }
            matrix[0][j] = i++;
        }
        System.out.println(Arrays.toString(matrix[0]));
        solve(matrix);
        return matrix;
    }

    private static int[][] manualGeneration() {
        int[][] matrix = new int[9][9];
        
        for (int i = 0; i < matrix.length; i++) {
            System.out.print("Row " + (i + 1) + ": ");
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = scanner.nextInt();
            }
        }
        return matrix;
    }

    private static boolean isSudokuValid(int[][] sudokuGrid) {
        for (int i = 0; i < sudokuGrid.length; i++) {
            for (int j = 0; j < sudokuGrid[i].length; j++) {
                if (!isValid(sudokuGrid, i, j, sudokuGrid[i][j])) {
                    return false;
                }
            }
        }
        return true;
    }

    private static int[][] copyGrid(int[][] original) {
        int[][] copy = new int[9][9];
        for (int i = 0; i < copy.length; i++) {
            for (int j = 0; j < copy[i].length; j++) {
                copy[i][j] = original[i][j];
            }
        }
        return copy;
    }

    private static void displaySudokuGrid(int[][] sudokuGrid) {
        String black = "\u001B[30m";
        String red = "\u001B[31m";
        String green = "\u001B[32m";
        String yellow = "\u001B[33m";
        String blue = "\u001B[34m";
        String magenta = "\u001B[35m";
        String cyan = "\u001B[36m";
        String white = "\u001B[37m";
        String brightWhite = "\u001B[97m";
        String reset = "\u001B[0m";

        System.out.println("Steps: " + stepCount);
        for (int i = 0; i < sudokuGrid.length; i++) {
            for (int j = 0; j < sudokuGrid[i].length; j++) {
                if (puzzle != null && puzzle[i][j] != sudokuGrid[i][j]) {
                    System.out.print("    " + green + sudokuGrid[i][j] + reset);
                } else if (sudokuGrid[i][j] == 0) {
                    System.out.print("    " + red + "-" + reset);
                } else {
                    System.out.print("    " + brightWhite + sudokuGrid[i][j] + reset);
                }
            }
            System.out.println("\n");
        }
    }
}