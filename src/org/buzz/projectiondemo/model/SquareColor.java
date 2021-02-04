package org.buzz.projectiondemo.model;

import javafx.scene.paint.Color;

public enum SquareColor {
    RED(Color.RED),
    BLUE(Color.BLUE),
    NONE(Color.LIGHTGRAY);

    private final Color color;

    SquareColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
