package com.sam.hab.gui;

import javafx.application.Application;
import javafx.embed.swt.FXCanvas;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class GUI extends Application {

    public static void start() {
        launch();
    }

    Map<String, Object> fxmlNamespace;

    @Override
    public void start(Stage primaryStage) {
        FXMLLoader loader = new FXMLLoader(GUI.class.getResource("UI.fxml"));

        SplitPane pane = null;
        try {
            pane = loader.load();
        } catch (IOException e) {
            System.out.println("ORANGES!");
            e.printStackTrace();
        }

        fxmlNamespace = loader.getNamespace();
        ((Text)fxmlNamespace.get("lat")).setText("51.200000");
        ((Text)fxmlNamespace.get("lon")).setText("51.200000");

        primaryStage.setScene(new Scene(pane, 1024, 768));

        primaryStage.setTitle("Kittens!");
        primaryStage.show();
    }

    public void setTest(String s) {
        ((Text)fxmlNamespace.get("telemsecs")).setText("51.200000");
    }
}
