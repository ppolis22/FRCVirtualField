package org.buzz.projectiondemo.controller;

import org.buzz.projectiondemo.model.Camera;
import org.buzz.projectiondemo.model.GameState;
import org.buzz.projectiondemo.model.ProcessResult;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;
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
    private boolean isProcessing = false;
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
            switch (appState) {
                case CALIBRATING -> calibrateToGrid();
                case FINDING_OBJECTS -> detectObjects();
            }
        } catch (Throwable e) {
            System.out.println("Throwable caught: ");
            e.printStackTrace();
        }
    }

    private void calibrateToGrid() {
        projectionController.showCalibrationImage();
        // locate markers, use frame processor?
        controlPanelController.writeDebugString("Searching for calibration image.");
        boolean calibrationImageRecognized = true;
        if (calibrationImageRecognized) {
            gameStateCalculator.calibrate(new Point(100, 100), new Point(700, 100),
                    new Point(100, 700), new Point(700, 700));
            appState = AppState.FINDING_OBJECTS;
        }
    }

    private void detectObjects() {
        Scalar minHsvValues = controlPanelController.getMinHsvValues();
        Scalar maxHsvValues = controlPanelController.getMaxHsvValues();
        ProcessResult result = frameProcessor.process(camera.getFrame(), minHsvValues, maxHsvValues);

        MatOfPoint2f[][] zones = gameStateCalculator.getBoardZones();
        GameState gameState = gameStateCalculator.calculate(result.contours);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                MatOfPoint mop = new MatOfPoint();
                zones[i][j].convertTo(mop, CvType.CV_32S);
                Imgproc.drawContours(result.mainMat, Arrays.asList(mop), -1, new Scalar(250, 0, 0));
            }
        }
        projectionController.updateProjectionView(gameState);

        controlPanelController.drawMainImage(result.mainMat);
        controlPanelController.drawThreshImage(result.threshMat);
        controlPanelController.drawDenoisedImage(result.denoiseMat);
        controlPanelController.writeDebugString(result.debugMessage);
    }

    private enum AppState {
        CALIBRATING,
        FINDING_OBJECTS
    }
}
