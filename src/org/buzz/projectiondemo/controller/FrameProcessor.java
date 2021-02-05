package org.buzz.projectiondemo.controller;

import org.buzz.projectiondemo.model.*;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FrameProcessor {

    public ProcessResult process(Mat input, Scalar minThreshVals, Scalar maxThreshVals) {
        ConvertableMat mainMat = new ConvertableMat();
        input.copyTo(mainMat);
        ConvertableMat threshMat = applyHSVFilter(mainMat, minThreshVals, maxThreshVals);
        ConvertableMat denoisedMat = denoiseImage(threshMat);
        List<Contour> contours = findContours(denoisedMat);
//        drawContours(contours, mainMat);
        return new ProcessResult(contours, mainMat, threshMat, denoisedMat,
                "Contours found: " + contours.size());
    }

    private ConvertableMat applyHSVFilter(Mat inputMat, Scalar minThreshVals, Scalar maxThreshVals) {
        Mat hsvMat = new Mat();
        ConvertableMat threshMat = new ConvertableMat();
        Imgproc.cvtColor(inputMat, hsvMat, Imgproc.COLOR_BGR2HSV);
        Core.inRange(hsvMat, minThreshVals, maxThreshVals, threshMat);
        return threshMat;
    }

    private ConvertableMat denoiseImage(Mat inputMat) {
        ConvertableMat deNoisedMat = new ConvertableMat();
        Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(24, 24));
        Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));

        Imgproc.erode(inputMat, deNoisedMat, erodeElement);
        Imgproc.erode(deNoisedMat, deNoisedMat, erodeElement);

        Imgproc.dilate(deNoisedMat, deNoisedMat, dilateElement);
        Imgproc.dilate(deNoisedMat, deNoisedMat, dilateElement);

        return deNoisedMat;
    }

    private List<Contour> findContours(Mat inputMat) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(inputMat, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours.stream().map(Contour::new).collect(Collectors.toList());
    }

    private void drawContours(List<Contour> contours, Mat toRenderOn) {
        if (!contours.isEmpty()) {
            for (int i = 0; i < contours.size(); i++) {
                Imgproc.drawContours(toRenderOn,
                        contours.subList(i, i + 1).stream().map(Contour::getMatOfPoint).collect(Collectors.toList()), -1, new Scalar(250, 0, 0));
            }
        }
    }
}
