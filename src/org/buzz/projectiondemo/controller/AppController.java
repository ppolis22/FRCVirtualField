package org.buzz.projectiondemo.controller;

import org.buzz.projectiondemo.model.Camera;
import org.buzz.projectiondemo.model.ProcessResult;
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
    private final FrameProcessor processor = new FrameProcessor();
    private boolean isProcessing = false;

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
        Scalar minValues = controlPanelController.getMinValues();
        Scalar maxValues = controlPanelController.getMaxValues();
        ProcessResult result = processor.process(camera.getFrame(), minValues, maxValues);
        projectionController.updateProjectionView(result.gameState);
        controlPanelController.drawMainImage(result.mainMat);
        controlPanelController.drawThreshImage(result.threshMat);
        controlPanelController.drawDenoisedImage(result.denoiseMat);
        controlPanelController.writeDebugString(result.debugMessage);
    }
}
