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
    public static final int MAIN_FRAME_DISPLAY_WIDTH = 640;
    private static final int MINOR_FRAME_DISPLAY_WIDTH = 320;

    @FXML private ImageView cameraFrame;
    @FXML private ImageView filteredImage1;
    @FXML private ImageView filteredImage2;
    @FXML private Button cameraButton;
    @FXML private Button continueButton;

    @FXML private Slider hueStartObj1;
    @FXML private Slider hueStopObj1;
    @FXML private Slider saturationStartObj1;
    @FXML private Slider saturationStopObj1;
    @FXML private Slider valueStartObj1;
    @FXML private Slider valueStopObj1;
    @FXML private CheckBox invertHueObj1;

    @FXML private Slider hueStartObj2;
    @FXML private Slider hueStopObj2;
    @FXML private Slider saturationStartObj2;
    @FXML private Slider saturationStopObj2;
    @FXML private Slider valueStartObj2;
    @FXML private Slider valueStopObj2;
    @FXML private CheckBox invertHueObj2;

    @FXML private Label debugOutput;

    @FXML
    public void initialize() {
        cameraFrame.setFitWidth(MAIN_FRAME_DISPLAY_WIDTH);
        cameraFrame.setPreserveRatio(true);
        cameraFrame.setOnMouseClicked(e -> appController.updateCornerPoint(e.getX(), e.getY()));
        filteredImage1.setFitWidth(MINOR_FRAME_DISPLAY_WIDTH);
        filteredImage1.setPreserveRatio(true);
        filteredImage2.setFitWidth(MINOR_FRAME_DISPLAY_WIDTH);
        filteredImage2.setPreserveRatio(true);
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

    public Scalar getMinHsvObj1Values() {
        return new Scalar(hueStartObj1.getValue(), saturationStartObj1.getValue(), valueStartObj1.getValue());
    }

    public Scalar getMaxHsvObj1Values() {
        return new Scalar(hueStopObj1.getValue(), saturationStopObj1.getValue(), valueStopObj1.getValue());
    }

    public Scalar getMinHsvObj2Values() {
        return new Scalar(hueStartObj2.getValue(), saturationStartObj2.getValue(), valueStartObj2.getValue());
    }

    public Scalar getMaxHsvObj2Values() {
        return new Scalar(hueStopObj2.getValue(), saturationStopObj2.getValue(), valueStopObj2.getValue());
    }

    public boolean invertObj1Hue() {
        return invertHueObj1.isSelected();
    }

    public boolean invertObj2Hue() {
        return invertHueObj2.isSelected();
    }

    public void drawBoardZones(MatOfPoint2f[][] zoneContours, ConvertableMat destinationMat) {
        for (MatOfPoint2f[] zoneRow : zoneContours) {
            for (MatOfPoint2f zone : zoneRow) {
                MatOfPoint mop = new MatOfPoint();
                zone.convertTo(mop, CvType.CV_32S);
                Imgproc.drawContours(destinationMat.getMat(), Arrays.asList(mop), -1, new Scalar(250, 0, 0), 2);
            }
        }
    }

    public void writeDebugString(String message) {
        updateFXProperty(debugOutputProp, message);
    }

    public void drawMainImage(ConvertableMat mat) {
        updateFXProperty(cameraFrame.imageProperty(), mat.asImage());
    }

    public void drawFilteredImage1(ConvertableMat mat) {
        updateFXProperty(filteredImage1.imageProperty(), mat.asImage());
    }

    public void drawFilteredImage2(ConvertableMat mat) {
        updateFXProperty(filteredImage2.imageProperty(), mat.asImage());
    }

    public void setContinueButtonState(boolean enabled) {
        continueButton.setDisable(!enabled);
    }
}
