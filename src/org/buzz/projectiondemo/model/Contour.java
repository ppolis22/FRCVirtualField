package org.buzz.projectiondemo.model;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

public class Contour implements Comparable<Contour> {

    private final MatOfPoint cvContour;
    private final Point centerPoint;
    private final double area;

    public Contour(MatOfPoint cvContour) {
        this.cvContour = cvContour;
        Moments moments = Imgproc.moments(cvContour);
        centerPoint = new Point(moments.get_m10() / moments.get_m00(), moments.get_m01() / moments.get_m00());
        area = moments.get_m00();
    }

    public Point getCenterPoint() {
        return centerPoint;
    }

    public double getArea() {
        return area;
    }

    public MatOfPoint getMatOfPoint() {
        return cvContour;
    }

    @Override
    public int compareTo(Contour o) {
        if (this.area > o.area) {
            return 1;
        } else if (this.area < o.area) {
            return -1;
        }
        return 0;
    }
}
