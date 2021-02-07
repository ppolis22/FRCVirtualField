package org.buzz.projectiondemo.controller;

import org.buzz.projectiondemo.model.Contour;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CalibrationSolver {

    public List<Point> getBoardCornerPoints(List<Contour> contours) {
        if (contours.size() > 20) {     // gotta narrow it down more
            return new ArrayList<>();
        }
        Collections.sort(contours);
        for (int i = 0; i < contours.size() - 3; i++) {
            List<Contour> potentials = contours.subList(i, i + 4);
            if (potentials.get(3).getArea() / potentials.get(0).getArea() > 0.5) {
                return orderPoints(potentials);    // also potential max/min size check?
            }
        }
        return new ArrayList<>();
    }

    private List<Point> orderPoints(List<Contour> cornerContours) {
        List<Contour> topMostPair, bottomMostPair;
        List<Point> clockWisePoints = new ArrayList<>();

        cornerContours.sort(Comparator.comparingDouble(c -> c.getCenterPoint().y));
        topMostPair = cornerContours.subList(0, 2);
        bottomMostPair = cornerContours.subList(2, 4);

        clockWisePoints.add(getLeftmost(topMostPair.get(0).getCenterPoint(), topMostPair.get(1).getCenterPoint()));
        clockWisePoints.add(getRightmost(topMostPair.get(0).getCenterPoint(), topMostPair.get(1).getCenterPoint()));
        clockWisePoints.add(getLeftmost(bottomMostPair.get(0).getCenterPoint(), bottomMostPair.get(1).getCenterPoint()));
        clockWisePoints.add(getRightmost(bottomMostPair.get(0).getCenterPoint(), bottomMostPair.get(1).getCenterPoint()));

        return clockWisePoints;
    }

    private Point getLeftmost(Point p1, Point p2) {
        return p1.x < p2.x ? p1 : p2;
    }

    private Point getRightmost(Point p1, Point p2) {
        return p1.x > p2.x ? p1 : p2;
    }

}
