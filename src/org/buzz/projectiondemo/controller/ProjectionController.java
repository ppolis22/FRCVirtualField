package org.buzz.projectiondemo.controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.buzz.projectiondemo.model.SquareColor;
import org.buzz.projectiondemo.model.GameState;

import java.util.ArrayList;
import java.util.List;

public class ProjectionController extends ViewController {
    private final int canvasWidth = 600;
    private final int canvasHeight = 600;
    private final List<CalibrationPoint> calibrationPoints = new ArrayList<>();
    @FXML private Canvas canvas;

    @FXML
    public void initialize() {
        canvas.setWidth(canvasWidth);
        canvas.setHeight(canvasHeight);
        int calPtSize = 25;
        calibrationPoints.add(new CalibrationPoint(0, 0, calPtSize, calPtSize, 0));
        calibrationPoints.add(new CalibrationPoint(canvasWidth - calPtSize, 0, calPtSize, calPtSize, 1));
        calibrationPoints.add(new CalibrationPoint(0, canvasHeight - calPtSize, calPtSize, calPtSize, 2));
        calibrationPoints.add(new CalibrationPoint(canvasWidth - calPtSize, canvasHeight - calPtSize, calPtSize, calPtSize, 3));
    }

    public void updateProjectionView(GameState gameState) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        SquareColor[][] board = gameState.getBoard();
        drawSquares(board, g);
        drawBoardLines(g);
    }

    public void showCalibrationImage(int numPlaced) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, canvasWidth, canvasHeight);
        for (CalibrationPoint point : calibrationPoints) {
            point.draw(g, numPlaced);
        }
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

    private static class CalibrationPoint {
        int x, y, width, height, seqNum;
        public CalibrationPoint(int x, int y, int width, int height, int seqNum) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.seqNum = seqNum;
        }

        public void draw(GraphicsContext g, int currentNum) {
            if (currentNum < seqNum) {
                g.setFill(Color.BLACK);
            } else if (currentNum == seqNum) {
                g.setFill(Color.RED);
            } else {
                g.setFill(Color.LIMEGREEN);
            }
            g.fillRect(x, y, width, height);
        }
    }
}
