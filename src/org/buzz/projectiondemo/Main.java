package org.buzz.projectiondemo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.buzz.projectiondemo.controller.AppController;
import org.buzz.projectiondemo.controller.ControlPanelController;
import org.buzz.projectiondemo.controller.ProjectionController;
import org.opencv.core.Core;

import static org.opencv.core.Core.NATIVE_LIBRARY_NAME;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("controlpanel.fxml"));
        Parent controlPanelRoot = loader.load();
        ControlPanelController controlPanelController = loader.getController();
        primaryStage.setTitle("Control Panel");
        primaryStage.setScene(new Scene(controlPanelRoot, 1200, 800));
        primaryStage.show();

        loader = new FXMLLoader(getClass().getResource("projection.fxml"));
        Parent projectionRoot = loader.load();
        Stage projectionStage = new Stage();
        ProjectionController projectionController = loader.getController();
        projectionStage.setTitle("Virtual Field");
        projectionStage.setScene(new Scene(projectionRoot, 800, 600));
        projectionStage.show();

        AppController appController = new AppController(controlPanelController, projectionController);
        appController.initialize();
    }


    public static void main(String[] args) {
        System.loadLibrary(NATIVE_LIBRARY_NAME);
        System.out.println("Loaded OpenCV version: " + Core.VERSION);
        launch(args);
    }
}
