package org.buzz.projectiondemo.model;

import java.util.Arrays;

public class GameState {
    private final SquareColor[][] board = new SquareColor[3][3];

    public GameState() {
        clearBoard();
    }

    public SquareColor[][] getBoard() {
        return board;
    }

    public void setSquareValue(SquareColor value, int row, int col) {
        board[row][col] = value;
    }

    private void clearBoard() {
        for (SquareColor[] row : board) {
            Arrays.fill(row, SquareColor.NONE);
        }
    }
}
