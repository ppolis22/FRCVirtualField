package org.buzz.projectiondemo.controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.buzz.projectiondemo.model.SquareColor;
import org.buzz.projectiondemo.model.GameState;

public class ProjectionController extends ViewController {
    private final int canvasWidth = 600;
    private final int canvasHeight = 600;
    @FXML private Canvas canvas;

    @FXML
    public void initialize() {
        canvas.setWidth(canvasWidth);
        canvas.setHeight(canvasHeight);
    }

    public void updateProjectionView(GameState gameState) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        SquareColor[][] board = gameState.getBoard();
        drawSquares(board, g);
        drawBoardLines(g);
    }

    public void showCalibrationImage() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        int markWidth = 25;
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, canvasWidth, canvasHeight);
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, markWidth, markWidth);
        g.fillRect(canvasWidth - markWidth, 0, markWidth, markWidth);
        g.fillRect(0, canvasHeight - markWidth, markWidth, markWidth);
        g.fillRect(canvasWidth - markWidth, canvasHeight - markWidth, markWidth, markWidth);
    }

    private void drawBoardLines(GraphicsContext g) {
        g.setFill(Color.BLACK);
        g.fillRect(195, 0, 10, 600);
        g.fillRect(395, 0, 10, 600);
        g.fillRect(0, 195, 600, 10);
        g.fillRect(0, 395, 600, 10);
    }

    private void drawSquares(SquareColor[][] board, GraphicsContext g) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                g.setFill(board[i][j].getColor());
                g.fillRect(j * 200, i * 200, 200, 200);
            }
        }
    }
}
