package org.buzz.projectiondemo.model;

import java.util.Arrays;

public class GameState {
    private final Color[][] board = new Color[3][3];

    public GameState() {
        clearBoard();
    }

    public Color[][] getBoard() {
        return board;
    }

    public void setSquareValue(Color value, int row, int col) {
        board[row][col] = value;
    }

    private void clearBoard() {
        for (Color[] row : board) {
            Arrays.fill(row, Color.NONE);
        }
    }
}
