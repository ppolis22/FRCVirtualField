package org.buzz.projectiondemo.controller;

import org.buzz.projectiondemo.model.*;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FrameProcessor {

    public ProcessResult process(Mat input, Scalar minThreshVals, Scalar maxThreshVals, boolean invertHue) {
        Mat mainMat = new Mat();
        input.copyTo(mainMat);
        Mat blurredMat = blur(mainMat);
        Mat threshMat = applyHSVFilter(blurredMat, minThreshVals, maxThreshVals, invertHue);
        Mat denoisedMat = denoiseImage(threshMat);
        List<Contour> contours = findContours(denoisedMat);
        return new ProcessResult(contours, new ConvertableMat(threshMat), new ConvertableMat(denoisedMat),
                "Contours found: " + contours.size());
    }

    private Mat blur(Mat inputMat) {
        Mat blurredMat = new Mat();
        Imgproc.blur(inputMat, blurredMat, new Size(5, 5));
        return blurredMat;
    }

    private Mat applyHSVFilter(Mat inputMat, Scalar minThreshVals, Scalar maxThreshVals, boolean invertHue) {
        Mat hsvMat = new Mat();
        Mat threshMat = new Mat();
        Imgproc.cvtColor(inputMat, hsvMat, Imgproc.COLOR_BGR2HSV);
        if (invertHue) {
            Mat threshMatLower = new Mat();
            Mat threshMatUpper = new Mat();
            Scalar zeroHuePoint = new Scalar(0.0, minThreshVals.val[1], minThreshVals.val[2]);
            Scalar firstMidPoint = new Scalar(minThreshVals.val[0], maxThreshVals.val[1], maxThreshVals.val[2]);
            Scalar secondMidPoint = new Scalar(maxThreshVals.val[0], minThreshVals.val[1], minThreshVals.val[2]);
            Scalar maxHuePoint = new Scalar(180.0, maxThreshVals.val[1], maxThreshVals.val[2]);
            Core.inRange(hsvMat, zeroHuePoint, firstMidPoint, threshMatLower);
            Core.inRange(hsvMat, secondMidPoint, maxHuePoint, threshMatUpper);
            Core.bitwise_or(threshMatLower, threshMatUpper, threshMat);
        } else {
            Core.inRange(hsvMat, minThreshVals, maxThreshVals, threshMat);
        }
        return threshMat;
    }

    private Mat denoiseImage(Mat inputMat) {
        Mat deNoisedMat = new Mat();
        Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(24, 24));
        Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));

        Imgproc.erode(inputMat, deNoisedMat, erodeElement);
        Imgproc.dilate(deNoisedMat, deNoisedMat, dilateElement);

        return deNoisedMat;
    }

    private List<Contour> findContours(Mat inputMat) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(inputMat, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours.stream().map(Contour::new).collect(Collectors.toList());
    }
}
