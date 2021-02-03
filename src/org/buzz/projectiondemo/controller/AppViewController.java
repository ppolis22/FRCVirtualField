package org.buzz.projectiondemo.controller;

import org.buzz.projectiondemo.model.ConvertableMat;

public interface AppViewController {
    void writeDebugString(String message);

    void drawMainImage(ConvertableMat image);

    void drawThreshImage(ConvertableMat image);

    void drawDenoisedImage(ConvertableMat image);
}
