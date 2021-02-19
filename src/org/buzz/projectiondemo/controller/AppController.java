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
        gameStateCalculator.setRandomTargetState();
        appState = AppState.FINDING_OBJECTS;
    }

    private void startProcessingLoop() {
        try {
            camera.start();
            Mat sampleFrame = camera.getFrame().getMat();
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
        ConvertableMat frame = camera.getFrame();
        int cornersPlaced = gameStateCalculator.getNumCornersPlaced();
        projectionController.showCalibrationImage(cornersPlaced);
        controlPanelController.setContinueButtonState(cornersPlaced == 4 && appState == AppState.CALIBRATING);
        controlPanelController.drawMainImage(frame);
    }

    private void detectObjects() {
        Scalar minHsvObj1Values = controlPanelController.getMinHsvObj1Values();
        Scalar maxHsvObj1Values = controlPanelController.getMaxHsvObj1Values();
        boolean invertObj1Hue = controlPanelController.invertObj1Hue();
        Scalar minHsvObj2Values = controlPanelController.getMinHsvObj2Values();
        Scalar maxHsvObj2Values = controlPanelController.getMaxHsvObj2Values();
        boolean invertObj2Hue = controlPanelController.invertObj2Hue();

        ConvertableMat cameraFrame = camera.getFrame();

        ProcessResult obj1Result = frameProcessor.process(cameraFrame.getMat(), minHsvObj1Values, maxHsvObj1Values, invertObj1Hue);
        ProcessResult obj2Result = frameProcessor.process(cameraFrame.getMat(), minHsvObj2Values, maxHsvObj2Values, invertObj2Hue);

        MatOfPoint2f[][] zones = gameStateCalculator.getBoardZones();
        controlPanelController.drawBoardZones(zones, cameraFrame);

        GameState gameState = gameStateCalculator.calculate(obj1Result.contours, obj2Result.contours);
        projectionController.updateProjectionView(gameState);

        String debugMessage = controlPanelController.getMinHsvObj2Values().toString() + " -> " +
                controlPanelController.getMaxHsvObj2Values().toString();

        controlPanelController.drawMainImage(cameraFrame);
        controlPanelController.drawFilteredImage1(obj1Result.denoiseMat);
        controlPanelController.drawFilteredImage2(obj2Result.denoiseMat);
        controlPanelController.writeDebugString(debugMessage);
    }

    private enum AppState {
        CALIBRATING,
        FINDING_OBJECTS
    }
}
