package org.buzz.projectiondemo.controller;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import org.buzz.projectiondemo.model.ConvertableMat;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

public class ControlPanelController extends ViewController {

    private ObjectProperty<String> debugOutputProp;
    public static final int MAIN_FRAME_DISPLAY_WIDTH = 320;
    private static final int MINOR_FRAME_DISPLAY_WIDTH = 320;

    @FXML private ImageView cameraFrame;
    @FXML private ImageView maskImage;
    @FXML private ImageView morphImage;
    @FXML private Button cameraButton;
    @FXML private Button continueButton;
    @FXML private Slider hueStart;
    @FXML private Slider hueStop;
    @FXML private Slider saturationStart;
    @FXML private Slider saturationStop;
    @FXML private Slider valueStart;
    @FXML private Slider valueStop;
    @FXML private CheckBox invertHue;
    @FXML private Label debugOutput;

    @FXML
    public void initialize() {
        cameraFrame.setFitWidth(MAIN_FRAME_DISPLAY_WIDTH);
        cameraFrame.setPreserveRatio(true);
        cameraFrame.setOnMouseClicked(e -> appController.updateCornerPoint(e.getX(), e.getY()));
        maskImage.setFitWidth(MINOR_FRAME_DISPLAY_WIDTH);
        maskImage.setPreserveRatio(true);
        morphImage.setFitWidth(MINOR_FRAME_DISPLAY_WIDTH);
        morphImage.setPreserveRatio(true);
        debugOutputProp = new SimpleObjectProperty<>();
        debugOutput.textProperty().bind(debugOutputProp);
    }

    @FXML
    private void toggleProcessingBtnPressed() {
        appController.toggleProcessingLoop();
    }

    @FXML
    private void continueButtonPressed() {
        appController.lockInCalibration();
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

    public boolean invertHue() {
        return invertHue.isSelected();
    }

    public void drawBoardZones(MatOfPoint2f[][] zoneContours, ConvertableMat destinationMat) {
        for (MatOfPoint2f[] zoneRow : zoneContours) {
            for (MatOfPoint2f zone : zoneRow) {
                MatOfPoint mop = new MatOfPoint();
                zone.convertTo(mop, CvType.CV_32S);
                Imgproc.drawContours(destinationMat, Arrays.asList(mop), -1, new Scalar(250, 0, 0), 2);
            }
        }
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

    public void setContinueButtonState(boolean enabled) {
        continueButton.setDisable(!enabled);
    }
}
