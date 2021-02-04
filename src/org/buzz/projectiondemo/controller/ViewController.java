package org.buzz.projectiondemo.controller;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;

public class ViewController {
    AppController appController;

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    public <T> void updateFXProperty(final ObjectProperty<T> property, final T value) {
        Platform.runLater(() -> property.set(value));
    }
}
