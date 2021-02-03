package org.buzz.projectiondemo.controller;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import org.buzz.projectiondemo.model.ConvertableMat;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.videoio.VideoCapture;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller implements AppViewController {
    private GameEngine engine;
    private ScheduledExecutorService executor;
    private VideoCapture videoCapture;
    private boolean isProcessing = false;
    private ObjectProperty<String> debugOutputProp;

    @FXML private ImageView cameraFrame;
    @FXML private ImageView maskImage;
    @FXML private ImageView morphImage;
    @FXML private Button cameraButton;
    @FXML private Slider hueStart;
    @FXML private Slider hueStop;
    @FXML private Slider saturationStart;
    @FXML private Slider saturationStop;
    @FXML private Slider valueStart;
    @FXML private Slider valueStop;
    @FXML private Label debugOutput;

    @FXML
    public void initialize() {
        cameraFrame.setFitWidth(300);
        cameraFrame.setPreserveRatio(true);
        maskImage.setFitWidth(300);
        maskImage.setPreserveRatio(true);
        morphImage.setFitWidth(300);
        morphImage.setPreserveRatio(true);
        debugOutputProp = new SimpleObjectProperty<>();
        debugOutput.textProperty().bind(debugOutputProp);

        videoCapture = new VideoCapture();
        engine = new GameEngine(this);
    }

    @FXML
    private void toggleProcessingLoop() {
        if (!isProcessing) {
            startProcessingLoop();
        } else {
            stopProcessingLoop();
        }
    }

    private void startProcessingLoop() {
        videoCapture.open(0);
        if (videoCapture.isOpened()) {
            isProcessing = true;
            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(this::processFeedAndRender, 0, 100, TimeUnit.MILLISECONDS);
            cameraButton.setText("Stop Camera");
        } else {
            System.out.print("Unable to open camera feed.");
        }
    }

    private void stopProcessingLoop() {
        if (executor != null && !executor.isShutdown()) {
            isProcessing = false;
            cameraButton.setText("Start Camera");
            try {
                executor.shutdown();
                executor.awaitTermination(101, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
                System.out.print("Unable to properly shut down. Attempting to release camera...");
            }

            if (videoCapture.isOpened()) {
                videoCapture.release();
            }
        }
    }

    private void processFeedAndRender() {
        Mat frame = new Mat();
        videoCapture.read(frame);
        Scalar minValues = new Scalar(hueStart.getValue(), saturationStart.getValue(), valueStart.getValue());
        Scalar maxValues = new Scalar(hueStop.getValue(), saturationStop.getValue(), valueStop.getValue());
        engine.process(frame, minValues, maxValues);
    }

    public void writeDebugString(String message) {
        updateFXProperty(debugOutputProp, message);
    }

    public void drawMainImage(ConvertableMat mat) {
        updateFXProperty(cameraFrame.imageProperty(), mat.asImage());
    }

    public void drawThreshImage(ConvertableMat mat) {
        updateFXProperty(maskImage.imageProperty(), mat.asImage());
    }

    public void drawDenoisedImage(ConvertableMat mat) {
        updateFXProperty(morphImage.imageProperty(), mat.asImage());
    }

    public <T> void updateFXProperty(final ObjectProperty<T> property, final T value) {
        Platform.runLater(() -> property.set(value));
    }
}
