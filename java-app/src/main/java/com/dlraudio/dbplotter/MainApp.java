package com.dlraudio.dbplotter;

import com.dlraudio.dbplotter.controller.ArduinoCommandController;
import com.dlraudio.dbplotter.util.SerialPortUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;

public class MainApp extends Application {

    private static final ArduinoCommandController arduinoController = new ArduinoCommandController();

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dlraudio/ui/main.fxml"));

        Parent root = loader.load();

        primaryStage.setTitle("dB Plotter Application - DLR Audio");
        Scene scene = new Scene(root, 800, 500);

        scene.getStylesheets().add(getClass().getResource("/com/dlraudio/ui/style.css").toExternalForm());

        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/com/dlraudio/ui/images/logo.png")));


        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            try {
                Taskbar taskbar = Taskbar.getTaskbar();
                ImageIcon icon = new ImageIcon(getClass().getResource("/com/dlraudio/ui/images/logo.png"));
                taskbar.setIconImage(icon.getImage());

            } catch (Exception e) {
                System.err.println("Could not set Dock icon: " + e.getMessage());
            }
        }


        primaryStage.setResizable(false);
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            closeApplication();
        });

        primaryStage.show();
    }

    public static void closeApplication() {
        System.out.println("Closing application...");

        if (arduinoController.isTransmissionOngoing()) {
            System.out.println("Stopping ongoing transmission...");
            arduinoController.stopTransmission();
        }

        if (SerialPortUtils.isConnected()) {
            System.out.println("Disconnecting serial port...");
            SerialPortUtils.disconnect();
        }

        System.exit(0);
    }

    public static void main(String[] args) {

        System.setProperty("javafx.application.name", "DB Plotter");
        launch(args);
    }
}
