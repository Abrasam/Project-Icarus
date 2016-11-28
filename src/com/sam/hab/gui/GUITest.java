package com.sam.hab.gui;

import javafx.application.Application;
import javafx.stage.Stage;

public class GUITest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Kittens!");
        primaryStage.show();
    }
}
