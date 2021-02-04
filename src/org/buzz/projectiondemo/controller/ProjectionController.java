package org.buzz.projectiondemo.controller;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import org.buzz.projectiondemo.model.GameState;

public class ProjectionController extends ViewController {
    @FXML private ImageView projectionImage;

    @FXML
    public void initialize() {
        projectionImage.setFitWidth(500);
        projectionImage.setPreserveRatio(true);
    }

    public void updateProjectionView(GameState gameState) {

    }
}
