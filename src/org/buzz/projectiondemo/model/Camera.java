package org.buzz.projectiondemo.model;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class Camera {

    private VideoCapture videoCapture = new VideoCapture();

    public void start() throws Exception {
        videoCapture.open(0);
        if (!videoCapture.isOpened()) {
            throw new Exception("Unable to open camera");
        }
    }

    public void stop() {
        if (videoCapture.isOpened()) {
            videoCapture.release();
        }
    }

    public Mat getFrame() {
        Mat frame = new Mat();
        videoCapture.read(frame);
        return frame;
    }
}
