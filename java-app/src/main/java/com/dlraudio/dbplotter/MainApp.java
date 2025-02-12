package com.dlraudio.dbplotter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dlraudio/ui/main.fxml"));

        Parent root = loader.load();

        primaryStage.setTitle("dB Plotter Application - DLR Audio");
        Scene scene = new Scene(root, 800, 500);

        scene.getStylesheets().add(getClass().getResource("/com/dlraudio/ui/style.css").toExternalForm());

        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/com/dlraudio/ui/images/logo.png")));

        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
