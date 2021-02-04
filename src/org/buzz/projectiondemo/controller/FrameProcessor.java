package org.buzz.projectiondemo.controller;

import org.buzz.projectiondemo.model.Color;
import org.buzz.projectiondemo.model.ConvertableMat;
import org.buzz.projectiondemo.model.GameState;
import org.buzz.projectiondemo.model.ProcessResult;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class FrameProcessor {

    private AppState state = AppState.CALIBRATING;

    public ProcessResult process(Mat input, Scalar minThreshVals, Scalar maxThreshVals) {
        switch (state) {
            case CALIBRATING -> { return calibrateToGrid(); }
            case FINDING_OBJECTS -> { return detectObjects(input, minThreshVals, maxThreshVals); }
            default -> { return ProcessResult.EMPTY; }
        }
    }

    private ProcessResult calibrateToGrid() {
        // TODO
        // project calibration image
        // locate markers
        // calculate virtual grid
        // project grid image
        state = AppState.FINDING_OBJECTS;
        return ProcessResult.EMPTY;
    }

    private ProcessResult detectObjects(Mat input, Scalar minThreshVals, Scalar maxThreshVals) {
        ConvertableMat mainMat = new ConvertableMat();
        input.copyTo(mainMat);
        ConvertableMat threshMat = applyHSVFilter(mainMat, minThreshVals, maxThreshVals);
        ConvertableMat denoisedMat = denoiseImage(threshMat);
        List<MatOfPoint> contours = findContours(denoisedMat);
        drawContours(contours, mainMat);
        GameState gameState = calculateGameState(contours);
        return new ProcessResult(gameState, mainMat, threshMat, denoisedMat,
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

    private List<MatOfPoint> findContours(Mat inputMat) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(inputMat, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;
    }

    private void drawContours(List<MatOfPoint> contours, Mat toRenderOn) {
        if (!contours.isEmpty()) {
            for (int i = 0; i < contours.size(); i++) {
                Imgproc.drawContours(toRenderOn, contours.subList(i, i + 1), -1, new Scalar(250, 0, 0));
            }
        }
    }

    private GameState calculateGameState(List<MatOfPoint> contours) {
        GameState gameState = new GameState();
        gameState.setSquareValue(Color.RED, 1, 1);
        return gameState;
    }

    private enum AppState {
        CALIBRATING,
        FINDING_OBJECTS
    }
}
