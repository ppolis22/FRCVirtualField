package org.buzz.projectiondemo.controller;

import org.buzz.projectiondemo.model.Camera;
import org.buzz.projectiondemo.model.ConvertableMat;
import org.buzz.projectiondemo.model.GameState;
import org.buzz.projectiondemo.model.ProcessResult;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

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
    private double cameraFrameDisplayRatio = 1.0;
    private AppState appState = AppState.CALIBRATING;

    public AppController(ControlPanelController controlPanelController,
                         ProjectionController projectionController) {
        this.controlPanelController = controlPanelController;
        this.projectionController = projectionController;
    }

    public void initialize() {
        controlPanelController.setAppController(this);
        projectionController.setAppController(this);
    }

    public void toggleProcessingLoop() {
        if (!isProcessing) {
            startProcessingLoop();
        } else {
            stopProcessingLoop();
        }
        controlPanelController.setCameraButtonStatus(isProcessing);
    }

    public void updateCornerPoint(double x, double y) {
        gameStateCalculator.addCornerPoint(new Point(x * cameraFrameDisplayRatio, y * cameraFrameDisplayRatio));
    }

    public void lockInCalibration() {
        controlPanelController.setContinueButtonState(false);
        gameStateCalculator.calibrateToSetPoints();
        appState = AppState.FINDING_OBJECTS;
    }

    private void startProcessingLoop() {
        try {
            camera.start();
            Mat sampleFrame = camera.getFrame();
            cameraFrameDisplayRatio = (double)sampleFrame.width() / (double)ControlPanelController.MAIN_FRAME_DISPLAY_WIDTH;
            System.out.println("Ratio: " + cameraFrameDisplayRatio);
            isProcessing = true;
            controlPanelController.writeDebugString("Click on the red corner to calibrate...");
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
        Mat frame = camera.getFrame();
        ConvertableMat mainMat = new ConvertableMat();
        frame.copyTo(mainMat);
        int cornersPlaced = gameStateCalculator.getNumCornersPlaced();
        projectionController.showCalibrationImage(cornersPlaced);
        controlPanelController.setContinueButtonState(cornersPlaced == 4 && appState == AppState.CALIBRATING);
        controlPanelController.drawMainImage(mainMat);
    }

    private void detectObjects() {
        Scalar minHsvValues = controlPanelController.getMinHsvValues();
        Scalar maxHsvValues = controlPanelController.getMaxHsvValues();
        boolean invertHue = controlPanelController.invertHue();
        ProcessResult result= frameProcessor.process(camera.getFrame(), minHsvValues, maxHsvValues, invertHue);

        MatOfPoint2f[][] zones = gameStateCalculator.getBoardZones();
        controlPanelController.drawBoardZones(zones, result.mainMat);

        GameState gameState = gameStateCalculator.calculate(result.contours);
        projectionController.updateProjectionView(gameState);

        result.debugMessage = controlPanelController.getMinHsvValues().toString() + " -> " +
                controlPanelController.getMaxHsvValues().toString();

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
