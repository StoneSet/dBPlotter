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
        // Charger le fichier FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dlraudio/ui/main.fxml"));

        // Charger la racine
        Parent root = loader.load();

        // Définir la scène et le titre de la fenêtre
        primaryStage.setTitle("DB Plotter Application");
        Scene scene = new Scene(root, 800, 500);

        // Ajouter la feuille de style
        scene.getStylesheets().add(getClass().getResource("/com/dlraudio/ui/style.css").toExternalForm());

        // Ajouter une icône
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/com/dlraudio/ui/logo.png")));

        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
