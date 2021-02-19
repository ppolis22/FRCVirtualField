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
        drawSquares(gameState, g);
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

    private void drawSquares(GameState gameState, GraphicsContext g) {
        SquareColor[][] targetBoard = gameState.getTargetBoard();
        SquareColor[][] detectedBoard = gameState.getDetectedBoard();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                SquareColor target = targetBoard[i][j];
                SquareColor detected = detectedBoard[i][j];
                if (target == detected) {
                    drawMatchSquare(j, i, target, g);
                } else {
                    drawMismatchSquare(j, i, target, detected, g);
                }
            }
        }
    }

    private void drawMatchSquare(int col, int row, SquareColor color, GraphicsContext g) {
        g.setFill(color.getColor());
        g.fillRect(col * 200, row * 200, 200, 200);
    }

    private void drawMismatchSquare(int col, int row, SquareColor targetColor,
                                    SquareColor detectedColor, GraphicsContext g) {
        g.setFill(targetColor.getColor());
        g.fillRect(col * 200, row * 200, 200, 200);
        g.setFill(SquareColor.NONE.getColor());
        g.fillRect(col * 200 + 25, row * 200 + 25, 150, 150);
        g.setFill(SquareColor.NONE.getColor());
        g.setStroke(detectedColor.getColor());
        g.setLineWidth(20);
        g.strokeLine(col * 200 + 50, row * 200 + 50, col * 200 + 150, row * 200 + 150);
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
