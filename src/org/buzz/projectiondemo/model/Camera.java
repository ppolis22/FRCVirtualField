package org.buzz.projectiondemo.model;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.io.IOException;

public class Camera {

    private final VideoCapture videoCapture = new VideoCapture();

    public void start() throws IOException {
        videoCapture.open(0);
        if (!videoCapture.isOpened()) {
            throw new IOException("Unable to open camera");
        }
    }

    public void stop() {
        if (videoCapture.isOpened()) {
            videoCapture.release();
        }
    }

    public ConvertableMat getFrame() {
        Mat frame = new Mat();
        videoCapture.read(frame);
        return new ConvertableMat(frame);
    }
}
