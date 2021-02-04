package org.buzz.projectiondemo.controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.buzz.projectiondemo.model.SquareColor;
import org.buzz.projectiondemo.model.GameState;

public class ProjectionController extends ViewController {
    @FXML private Canvas canvas;

    @FXML
    public void initialize() {
        canvas.setWidth(600);
        canvas.setHeight(600);
    }

    public void updateProjectionView(GameState gameState) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        SquareColor[][] board = gameState.getBoard();
        drawSquares(board, g);
        drawBoardLines(g);
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
                g.fillRect(i * 200, j * 200, 200, 200);
            }
        }
    }
}
