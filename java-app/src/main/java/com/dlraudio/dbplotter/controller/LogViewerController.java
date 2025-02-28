package com.dlraudio.dbplotter.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.application.Platform;

public class LogViewerController {

    @FXML
    private TextArea logTextArea;

    private static LogViewerController instance;

    public LogViewerController() {
        instance = this;
    }

    public static LogViewerController getInstance() {
        return instance;
    }

    @FXML
    public void clearLog() {
        logTextArea.clear();
    }

    @FXML
    public void closeWindow() {
        Stage stage = (Stage) logTextArea.getScene().getWindow();
        stage.close();
    }

    public void appendLog(String message) {
        Platform.runLater(() -> {
            if (logTextArea != null && message != null) {
                logTextArea.appendText(message + "\n");
                logTextArea.positionCaret(logTextArea.getText().length());
            }
        });
    }
}
