package org.buzz.projectiondemo.controller;

import org.buzz.projectiondemo.model.Contour;
import org.buzz.projectiondemo.model.GameState;
import org.buzz.projectiondemo.model.SquareColor;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class GameStateCalculator {

    private final static double ONE_THIRD = 1.0 / 3.0;
    private final static double TWO_THIRDS = 2.0 / 3.0;
    private final MatOfPoint2f[][] boardZones = new MatOfPoint2f[3][3];

    public void calibrate(Point topLeft, Point topRight, Point bottomLeft, Point bottomRight) {
        Point[][] gridPoints = computeGridPoints(topLeft, topRight, bottomLeft, bottomRight);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                boardZones[i][j] = new MatOfPoint2f(
                        gridPoints[i][j], gridPoints[i][j + 1],
                        gridPoints[i + 1][j + 1], gridPoints[i + 1][j]);
            }
        }
    }

    public GameState calculate(List<Contour> contours) {
        GameState gameState = new GameState();
        try {
            Contour largestContour = Collections.max(contours);
            Point centerPoint = largestContour.getCenterPoint();
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    double testResult = Imgproc.pointPolygonTest(boardZones[i][j], centerPoint, false);
                    if (testResult > 0) {
                        gameState.setSquareValue(SquareColor.RED, i, j);
                        return gameState;
                    }
                }
            }
        } catch (NoSuchElementException e) {
            // we can safely eat this
        }
        return gameState;
    }

    public MatOfPoint2f[][] getBoardZones() {
        return boardZones;
    }

    private Point calculatePointAlongLine(Point start, Point end, double percent) {
        double dy = end.y - start.y;
        double dx = end.x - start.x;
        return new Point(start.x + (dx * percent), start.y + (dy * percent));
    }

    private Point[][] computeGridPoints(Point topLeft, Point topRight, Point bottomLeft, Point bottomRight) {
        Point[][] gridPoints = new Point[4][4];
        gridPoints[0][0] = topLeft;
        gridPoints[0][3] = topRight;
        gridPoints[3][0] = bottomLeft;
        gridPoints[3][3] = bottomRight;

        gridPoints[0][1] = calculatePointAlongLine(topLeft, topRight, ONE_THIRD);
        gridPoints[0][2] = calculatePointAlongLine(topLeft, topRight, TWO_THIRDS);

        gridPoints[3][1] = calculatePointAlongLine(bottomLeft, bottomRight, ONE_THIRD);
        gridPoints[3][2] = calculatePointAlongLine(bottomLeft, bottomRight, TWO_THIRDS);

        gridPoints[1][0] = calculatePointAlongLine(topLeft, bottomLeft, ONE_THIRD);
        gridPoints[2][0] = calculatePointAlongLine(topLeft, bottomLeft, TWO_THIRDS);

        gridPoints[1][3] = calculatePointAlongLine(topRight, bottomRight, ONE_THIRD);
        gridPoints[2][3] = calculatePointAlongLine(topRight, bottomRight, TWO_THIRDS);

        gridPoints[1][1] = calculatePointAlongLine(gridPoints[1][0], gridPoints[1][3], ONE_THIRD);
        gridPoints[1][2] = calculatePointAlongLine(gridPoints[1][0], gridPoints[1][3], TWO_THIRDS);
        gridPoints[2][1] = calculatePointAlongLine(gridPoints[2][0], gridPoints[2][3], ONE_THIRD);
        gridPoints[2][2] = calculatePointAlongLine(gridPoints[2][0], gridPoints[2][3], TWO_THIRDS);

        return gridPoints;
    }

}
