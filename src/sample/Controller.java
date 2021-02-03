package sample;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller {
    private ScheduledExecutorService executor;
    private VideoCapture videoCapture;
    private boolean cameraActive = false;
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
    private void toggleCameraFeed() {
        cameraFrame.setFitWidth(300);
        cameraFrame.setPreserveRatio(true);
        maskImage.setFitWidth(300);
        maskImage.setPreserveRatio(true);
        morphImage.setFitWidth(300);
        morphImage.setPreserveRatio(true);
        debugOutputProp = new SimpleObjectProperty<>();
        debugOutput.textProperty().bind(debugOutputProp);

        videoCapture = new VideoCapture();

        if (!cameraActive) {
            cameraActive = true;
            startCameraFeed();
        } else {
            cameraActive = false;
            stopCameraFeed();
        }
    }

    private void startCameraFeed() {
        videoCapture.open(0);
        if (videoCapture.isOpened()) {
            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(this::processAndDisplayFrame, 0, 100, TimeUnit.MILLISECONDS);
            cameraButton.setText("Stop Camera");
        } else {
            System.out.print("Unable to open camera feed.");
        }
    }

    private void stopCameraFeed() {
        if (executor != null && !executor.isShutdown()) {
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

    private void processAndDisplayFrame() {
        Mat frame = getFrame();
        Mat threshMat = applyHSVFilter(frame);
        Mat denoisedMat = denoiseImage(threshMat);
        findAndDrawContours(denoisedMat, frame);

        Image rawImage = convertMat(frame);
        Image threshImage = convertMat(threshMat);
        Image denoisedImage = convertMat(denoisedMat);
        onFXThread(cameraFrame.imageProperty(), rawImage);
        onFXThread(maskImage.imageProperty(), threshImage);
        onFXThread(morphImage.imageProperty(), denoisedImage);
    }

    private Mat getFrame() {
        Mat readFrame = new Mat();
        videoCapture.read(readFrame);
        return readFrame;
    }

    private Mat denoiseImage(Mat inputMat) {
        Mat deNoisedMat = new Mat();
        Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(24, 24));
        Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));

        Imgproc.erode(inputMat, deNoisedMat, erodeElement);
        Imgproc.erode(deNoisedMat, deNoisedMat, erodeElement);

        Imgproc.dilate(deNoisedMat, deNoisedMat, dilateElement);
        Imgproc.dilate(deNoisedMat, deNoisedMat, dilateElement);

        return deNoisedMat;
    }

    private Mat applyHSVFilter(Mat inputMat) {
        Mat hsvMat = new Mat();
        Mat threshMat = new Mat();
        Imgproc.cvtColor(inputMat, hsvMat, Imgproc.COLOR_BGR2HSV);
        Scalar minValues = new Scalar(hueStart.getValue(), saturationStart.getValue(), valueStart.getValue());
        Scalar maxValues = new Scalar(hueStop.getValue(), saturationStop.getValue(), valueStop.getValue());
        Core.inRange(hsvMat, minValues, maxValues, threshMat);
        return threshMat;
    }

    private Image convertMat(Mat inputMat) {
        try {
            return SwingFXUtils.toFXImage(matToBufferedImage(inputMat), null);
        } catch (Exception e) {
            System.out.println("Cannot convert the Mat object: " + e);
            return null;
        }
    }

    private void findAndDrawContours(Mat inputMat, Mat outputMat) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(inputMat, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        onFXThread(debugOutputProp, "Contours found: " + contours.size());

        if (!contours.isEmpty()) {
            for (int i = 0; i < contours.size(); i++) {
                Imgproc.drawContours(outputMat, contours.subList(i, i + 1), -1, new Scalar(250, 0, 0));
            }
        }
    }

    public static <T> void onFXThread(final ObjectProperty<T> property, final T value) {
        Platform.runLater(() -> property.set(value));
    }

    private static BufferedImage matToBufferedImage(Mat original) {
        BufferedImage image;
        int width = original.width(), height = original.height(), channels = original.channels();
        byte[] sourcePixels = new byte[width * height * channels];
        original.get(0, 0, sourcePixels);

        if (original.channels() > 1) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        }
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

        return image;
    }
}
