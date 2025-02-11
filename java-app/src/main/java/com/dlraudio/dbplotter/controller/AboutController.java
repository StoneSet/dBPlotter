package com.dlraudio.dbplotter.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class AboutController {

    @FXML
    private Button closeButton;

    @FXML
    private ImageView logoImageView;

    @FXML
    public void initialize() {
        Image logoImage = new Image(getClass().getResource("/com/dlraudio/ui/images/bk2306.png").toExternalForm());
        logoImageView.setImage(logoImage);
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void onOpenMailLink() {
        try {
            java.awt.Desktop.getDesktop().mail(new java.net.URI("mailto:hello@valentinderouet.fr"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void onOpenWebsiteLink() {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI("https://dlr-audio.com"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onOpenLicenseLink(ActionEvent actionEvent) {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI("https://opensource.org/licenses/MIT"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onOpenGithubLink(ActionEvent actionEvent) {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI("https://github.com/StoneSet/dbPlotter"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
