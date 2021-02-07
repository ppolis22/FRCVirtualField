package org.buzz.projectiondemo.controller;

import org.buzz.projectiondemo.model.Camera;
import org.buzz.projectiondemo.model.GameState;
import org.buzz.projectiondemo.model.ProcessResult;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AppController {
    private final ControlPanelController controlPanelController;
    private final ProjectionController projectionController;
    private ScheduledExecutorService executor;
    private final Camera camera = new Camera();
    private final FrameProcessor frameProcessor = new FrameProcessor();
    private final GameStateCalculator gameStateCalculator = new GameStateCalculator();
    private final CalibrationSolver calibrationSolver = new CalibrationSolver();
    private boolean isProcessing = false;
    private List<Point> calibrationPoints = new ArrayList<>();
    private AppState appState = AppState.CALIBRATING;

    public AppController(ControlPanelController controlPanelController,
                         ProjectionController projectionController) {
        this.controlPanelController = controlPanelController;
        this.projectionController = projectionController;

        this.controlPanelController.setAppController(this);
        this.projectionController.setAppController(this);
    }

    public void toggleProcessingLoop() {
        if (!isProcessing) {
            startProcessingLoop();
        } else {
            stopProcessingLoop();
        }
        controlPanelController.setCameraButtonStatus(isProcessing);
    }

    public void lockInCalibration() {
        controlPanelController.setContinueButtonState(false);
        gameStateCalculator.calibrate(calibrationPoints.get(0), calibrationPoints.get(1),
                calibrationPoints.get(2), calibrationPoints.get(3));
        appState = AppState.FINDING_OBJECTS;
    }

    private void startProcessingLoop() {
        try {
            camera.start();
            isProcessing = true;
            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(this::process, 0, 100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            System.out.print(e.getMessage());
            stopProcessingLoop();
        }
    }

    private void stopProcessingLoop() {
        if (executor != null && !executor.isShutdown()) {
            isProcessing = false;
            try {
                executor.shutdown();
                executor.awaitTermination(101, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
                System.out.print("Unable to properly shut down. Attempting to release camera...");
            }
            camera.stop();
        }
    }

    private void process() {
        try {
            Scalar minHsvValues = controlPanelController.getMinHsvValues();
            Scalar maxHsvValues = controlPanelController.getMaxHsvValues();
            ProcessResult result= frameProcessor.process(camera.getFrame(), minHsvValues, maxHsvValues);
            switch (appState) {
                case CALIBRATING -> calibrateToGrid(result);
                case FINDING_OBJECTS -> detectObjects(result);
            }
            controlPanelController.drawMainImage(result.mainMat);
            controlPanelController.drawThreshImage(result.threshMat);
            controlPanelController.drawDenoisedImage(result.denoiseMat);
            controlPanelController.writeDebugString(result.debugMessage);
        } catch (Throwable e) {
            System.out.println("Throwable caught: ");
            e.printStackTrace();
        }
    }

    private void calibrateToGrid(ProcessResult result) {
        result.debugMessage = "Searching for calibration image.";
        projectionController.showCalibrationImage();
        List<Point> corners = calibrationSolver.getBoardCornerPoints(result.contours);
        if (!corners.isEmpty()) {
            controlPanelController.setContinueButtonState(true);
            calibrationPoints = corners;
            controlPanelController.drawCalibrationPoints(result.mainMat, calibrationPoints);
        }
    }

    private void detectObjects(ProcessResult result) {
        MatOfPoint2f[][] zones = gameStateCalculator.getBoardZones();
        controlPanelController.drawBoardZones(zones, result.mainMat);

        GameState gameState = gameStateCalculator.calculate(result.contours);
        projectionController.updateProjectionView(gameState);
    }

    private enum AppState {
        CALIBRATING,
        FINDING_OBJECTS
    }
}
