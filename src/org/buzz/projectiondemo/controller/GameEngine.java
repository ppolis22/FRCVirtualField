package org.buzz.projectiondemo.controller;

import org.buzz.projectiondemo.model.ConvertableMat;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class GameEngine {

    private AppState state = AppState.CALIBRATING;
    private final AppViewController viewController;

    public GameEngine(AppViewController viewController) {
        this.viewController = viewController;
    }

    public void process(Mat input, Scalar minThreshVals, Scalar maxThreshVals) {
        switch (state) {
            case CALIBRATING -> calibrateToGrid();
            case FINDING_OBJECTS -> processObjects(input, minThreshVals, maxThreshVals);
        }
    }

    private void calibrateToGrid() {
        // TODO
        state = AppState.FINDING_OBJECTS;
    }

    private void processObjects(Mat input, Scalar minThreshVals, Scalar maxThreshVals) {
        ConvertableMat mainMat = new ConvertableMat();
        input.copyTo(mainMat);
        ConvertableMat threshMat = applyHSVFilter(mainMat, minThreshVals, maxThreshVals);
        ConvertableMat denoisedMat = denoiseImage(threshMat);
        findAndDrawContours(denoisedMat, mainMat);
        viewController.drawMainImage(mainMat);
        viewController.drawThreshImage(threshMat);
        viewController.drawDenoisedImage(denoisedMat);
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

    private void findAndDrawContours(Mat inputMat, Mat toRenderOn) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(inputMat, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
        viewController.writeDebugString("Contours found: " + contours.size());

        if (!contours.isEmpty()) {
            for (int i = 0; i < contours.size(); i++) {
                Imgproc.drawContours(toRenderOn, contours.subList(i, i + 1), -1, new Scalar(250, 0, 0));
            }
        }
    }

    private enum AppState {
        CALIBRATING,
        FINDING_OBJECTS
    }
}
