package org.buzz.projectiondemo.model;

import java.util.Arrays;

public class GameState {
    private final SquareColor[][] targetBoard = new SquareColor[3][3];
    private final SquareColor[][] detectedBoard = new SquareColor[3][3];

    public GameState() {
        clearBoard(targetBoard);
        clearBoard(detectedBoard);
    }

    public SquareColor[][] getTargetBoard() {
        return targetBoard;
    }

    public SquareColor[][] getDetectedBoard() {
        return detectedBoard;
    }

    public void clearDetectedBoard() {
        clearBoard(detectedBoard);
    }

    public void setDetectedValue(SquareColor value, int row, int col) {
        detectedBoard[row][col] = value;
    }

    public void randomizeTargetBoard() {
        for (int i = 0; i < targetBoard.length; i++) {
            for (int j = 0; j < targetBoard[i].length; j++) {
                int randInt = (int)(Math.random() * 2.0);
                targetBoard[i][j] = randInt % 2 == 0 ? SquareColor.RED : SquareColor.BLUE;
            }
        }
    }

    private void clearBoard(SquareColor[][] board) {
        for (SquareColor[] row : board) {
            Arrays.fill(row, SquareColor.NONE);
        }
    }
}
