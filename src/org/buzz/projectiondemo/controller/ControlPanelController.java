package org.buzz.projectiondemo.controller;

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

public class ControlPanelController extends ViewController {

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
    }

    @FXML
    private void toggleProcessingBtnPressed() {
        appController.toggleProcessingLoop();
    }

    public void setCameraButtonStatus(boolean isProcessing) {
        if (isProcessing) {
            cameraButton.setText("Stop Camera");
        } else {
            cameraButton.setText("Start Camera");
        }
    }

    public Scalar getMinHsvValues() {
        return new Scalar(hueStart.getValue(), saturationStart.getValue(), valueStart.getValue());
    }

    public Scalar getMaxHsvValues() {
        return new Scalar(hueStop.getValue(), saturationStop.getValue(), valueStop.getValue());
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
}
