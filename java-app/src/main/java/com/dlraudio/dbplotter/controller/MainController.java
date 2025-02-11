package com.dlraudio.dbplotter.controller;

import com.dlraudio.dbplotter.model.FrequencyData;
import com.dlraudio.dbplotter.model.PlotParameters;
import com.dlraudio.dbplotter.service.PlottingService;
import com.dlraudio.dbplotter.test.AutoCalibrateTest;
import com.dlraudio.dbplotter.util.CsvImporter;
import com.dlraudio.dbplotter.util.FileUtils;
import com.dlraudio.dbplotter.util.SerialPortUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainController {

    @FXML
    public MenuItem smoothingNoneMenuItem;
    @FXML
    public Label currentFileLabel;
    @FXML
    private Menu selectComPortMenu;

    @FXML
    private MenuItem connectMenuItem;

    @FXML
    private MenuItem disconnectMenuItem;

    @FXML
    private Label statusLabel;

    @FXML
    private Label minFrequencyField;

    @FXML
    private Label maxFrequencyField;

    @FXML
    private Label minDbField;

    @FXML
    private Label maxDbField;

    @FXML
    private Label paperSpeedField;

    @FXML
    private Label portLabel;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private LineChart<Number, Number> lineChart;

    @FXML
    private MenuItem smoothing1OctaveMenuItem;
    @FXML
    private MenuItem smoothingHalfOctaveMenuItem;
    @FXML
    private MenuItem smoothingThirdOctaveMenuItem;
    @FXML
    private MenuItem smoothingSixthOctaveMenuItem;
    @FXML
    private MenuItem smoothingTwelfthOctaveMenuItem;
    @FXML
    private MenuItem smoothingTwentyFourthOctaveMenuItem;
    @FXML
    private MenuItem smoothingFortyEighthOctaveMenuItem;
    @FXML
    private Button sendTo2306Button;
    @FXML
    private Button stopButton;
    @FXML
    private Button paperPushButton;
    @FXML
    private Button autoCalibrateButton;

    private boolean isCsvImported = false;

    private String selectedPort;
    //for test purpose
    private boolean isConnected = true;


    private PlottingService plottingService;
    private PlotParameters plotParameters;

    @FXML
    public void initialize() {
        connectMenuItem.setDisable(true);
        disconnectMenuItem.setDisable(true);
        disableSmoothingMenus(true);
        updateButtonStates();

        plottingService = new PlottingService(lineChart);
        plottingService.initializePlot("Frequency (Hz)", "Amplitude (dB)");

        arduinoController.setProgressListener(progress ->
                Platform.runLater(() -> progressBar.setProgress(progress))
        );
    }

    private void updateButtonStates() {
        //boolean enableArduinoActions = SerialPortUtils.isConnected();
        //test purpose
        boolean enableArduinoActions = true;
        boolean enableSendTo2306 = enableArduinoActions && isCsvImported;

        sendTo2306Button.setDisable(!enableSendTo2306);
        stopButton.setDisable(!enableArduinoActions);
        paperPushButton.setDisable(!enableArduinoActions);
        autoCalibrateButton.setDisable(!enableArduinoActions);
    }


        private void disableSmoothingMenus(boolean disable) {
        smoothingNoneMenuItem.setDisable(disable);
        smoothing1OctaveMenuItem.setDisable(disable);
        smoothingHalfOctaveMenuItem.setDisable(disable);
        smoothingThirdOctaveMenuItem.setDisable(disable);
        smoothingSixthOctaveMenuItem.setDisable(disable);
        smoothingTwelfthOctaveMenuItem.setDisable(disable);
        smoothingTwentyFourthOctaveMenuItem.setDisable(disable);
        smoothingFortyEighthOctaveMenuItem.setDisable(disable);
    }

    private void updateCalculatedParameters(List<FrequencyData> dataPoints) {
        double minFreq = FrequencyData.getMinFrequency(dataPoints);
        double maxFreq = FrequencyData.getMaxFrequency(dataPoints);
        double minDb = FrequencyData.getMinMagnitude(dataPoints);
        double maxDb = FrequencyData.getMaxMagnitude(dataPoints);

        // Calcul et stockage des paramètres dans PlotParameters
        plotParameters = new PlotParameters(minFreq, maxFreq, minDb, maxDb, 0.0);
        // Mise à jour de l'interface utilisateur
        displayCalculatedParameters();
    }

    private void displayCalculatedParameters() {
        if (plotParameters != null) {
            minFrequencyField.setText(String.format("%.2f Hz", plotParameters.getMinFrequency()));
            maxFrequencyField.setText(String.format("%.2f Hz", plotParameters.getMaxFrequency()));
            minDbField.setText(String.format("%.2f dB", plotParameters.getMinDb()));
            maxDbField.setText(String.format("%.2f dB", plotParameters.getMaxDb()));
        }
    }

    @FXML
    public void onSelectComPortMenuShown() {
        selectComPortMenu.getItems().clear();
        String[] availablePorts = SerialPortUtils.listAvailablePorts();

        if (availablePorts.length == 0) {
            MenuItem noPortsItem = new MenuItem("No ports available");
            noPortsItem.setDisable(true);
            selectComPortMenu.getItems().add(noPortsItem);
        } else {
            for (String port : availablePorts) {
                MenuItem portItem = new MenuItem(port);
                portItem.setOnAction(event -> onPortSelected(port));
                selectComPortMenu.getItems().add(portItem);
            }
        }
    }

    private void onPortSelected(String port) {
        selectedPort = port;
        portLabel.setText(port);
        connectMenuItem.setDisable(false);
        System.out.println("Selected port: " + port);
    }

    private ArduinoCommandController arduinoController = new ArduinoCommandController();

    @FXML
    public void onConnect() {
        if (selectedPort != null && SerialPortUtils.connect(selectedPort, 115200)) {
            arduinoController.setupPort(selectedPort);
            statusLabel.setText("Connected");
            System.out.println("Connected to " + selectedPort);
        } else {
            statusLabel.setText("Failed to connect.");
            System.err.println("Connection failed.");
        }
        updateButtonStates();
    }

    @FXML
    public void onDisconnect() {
        SerialPortUtils.disconnect();
        isConnected = false;
        updateButtonStates();
        statusLabel.setText("Disconnected.");
        connectMenuItem.setDisable(false);
        disconnectMenuItem.setDisable(true);
        System.out.println("Disconnected.");
    }

    @FXML
    public void onImportCsvRew() {
        importCsvData("rew");
    }

    @FXML
    public void onImportCsvArta() {
        importCsvData("arta");
    }

    @FXML
    private void importCsvData(String type) {
        File csvFile = FileUtils.selectCsvFile(getWindow());
        if (csvFile != null) {
            CsvImporter csvImportService = new CsvImporter();
            List<FrequencyData> dataPoints;

            // Importation des données CSV selon le type de fichier
            if (type.equalsIgnoreCase("rew")) {
                dataPoints = csvImportService.importFromRew(csvFile);
            } else {
                dataPoints = csvImportService.importFromArta(csvFile);
            }

            // Vérification et affichage des données brutes
            if (dataPoints != null && !dataPoints.isEmpty()) {
                plottingService.plotData(dataPoints);

                // Mise à jour des paramètres calculés
                updateCalculatedParameters(dataPoints);

                // Activer les menus de smoothing
                disableSmoothingMenus(false);

                currentFileLabel.setText(csvFile.getName());
                isCsvImported = true;  // Fichier CSV importé
                updateButtonStates();  // Mise à jour des boutons
                // Mise à jour du statut
                statusLabel.setText("CSV data imported and plotted.");

                // Stocker les données brutes dans le service pour un éventuel lissage
                plottingService.setCurrentData(dataPoints);
            } else {
                statusLabel.setText("No data found in the CSV file.");
            }
        }
    }

    public void onApplySmoothingNone() {
        List<FrequencyData> originalData = plottingService.getOriginalData();
        if (originalData == null || originalData.isEmpty()) {
            statusLabel.setText("No data available for smoothing.");
            return;
        }

        plottingService.plotData(originalData);
        statusLabel.setText("Removed smoothing.");
    }

    // Lissage basé sur des fractions d'octave
    @FXML
    private void onApplySmoothing1Octave() {
        applySmoothing(1);
    }

    @FXML
    private void onApplySmoothingHalfOctave() {
        applySmoothing(2);
    }

    @FXML
    private void onApplySmoothingThirdOctave() {
        applySmoothing(3);
    }

    @FXML
    private void onApplySmoothingSixthOctave() {
        applySmoothing(6);
    }

    @FXML
    private void onApplySmoothingTwelfthOctave() {
        applySmoothing(12);
    }

    @FXML
    private void onApplySmoothingTwentyFourthOctave() {
        applySmoothing(24);
    }

    @FXML
    private void onApplySmoothingFortyEighthOctave() {
        applySmoothing(48);
    }

    @FXML
    private void applySmoothing(double octaveFraction) {
        List<FrequencyData> originalData = plottingService.getOriginalData();  // Récupérer les données brutes
        if (originalData == null || originalData.isEmpty()) {
            statusLabel.setText("No data available for smoothing.");
            return;
        }

        // Appliquer le lissage sans modifier les données originales
        List<FrequencyData> smoothedData = FrequencyData.smoothByOctave(originalData, octaveFraction);
        plottingService.plotData(smoothedData);
        statusLabel.setText(String.format("Applied %d/%d octave smoothing.", (int) octaveFraction, 1));
    }

    @FXML
    public void onSendTo2306() {
        if (isConnected) {
            // Récupération des données actuelles du graphe
            List<FrequencyData> dataPoints = plottingService.getCurrentData();
            if (dataPoints == null || dataPoints.isEmpty()) {
                System.err.println("No data available for transmission.");
                return;
            }

            // Calcul de la durée réelle basée sur les données
            double estimatedDurationSec = FrequencyData.getTotalDuration(dataPoints);
            arduinoController.updatePrintParameters(dataPoints.size(), estimatedDurationSec);

            // Obtenir la vitesse du papier mise à jour
            double paperSpeedMmPerSec = arduinoController.getPaperSpeed();
            if (paperSpeedMmPerSec <= 0) {
                System.err.println("Invalid paper speed: " + paperSpeedMmPerSec + " mm/s. Cannot proceed.");
                return;
            }

            paperSpeedField.setText(String.format("%.2f mm/s", arduinoController.getPaperSpeed()));

            // Démarrer la machine
            arduinoController.startMotor();

            // Initialiser la barre de progression et le statut
            Platform.runLater(() -> {
                progressBar.setProgress(0);
                statusLabel.setText("Sending data...");
            });

            // 🔥 Lancer la transmission avec la vitesse du papier mise à jour
            arduinoController.startDataTransmission(dataPoints, paperSpeedMmPerSec);
        }
    }

    private Window getWindow() {
        return statusLabel.getScene().getWindow();
    }

    @FXML
    public void onExit() {
        System.exit(0);
    }

    @FXML
    private void onStop() {
        if (isConnected) {
            arduinoController.stopTransmission();
            arduinoController.stopMotor();
            System.out.println("Stop command sent to Arduino.");
            statusLabel.setText("Machine stopped.");
        } else {
            System.err.println("Cannot stop: Arduino is not connected.");
        }
    }

    @FXML
    private void onPaperPush() {
        if (isConnected) {
            arduinoController.sendCommand("PAPER_PUSH");
            System.out.println("Paper Push command sent.");
            statusLabel.setText("Paper advancing...");
        } else {
            System.err.println("Cannot push paper: Arduino is not connected.");
        }
    }

    @FXML
    private void onAutoCalibrate() {
        if (isConnected) {
            System.out.println("Starting Auto Calibration...");
            statusLabel.setText("Auto Calibrating...");

            // 🔥 Mise à jour des paramètres de la calibration
            int totalPoints = 100;  // Nombre de points pour l'onde sinusoïdale
            double estimatedDurationSec = 10.0; // Ex: 10s pour parcourir l'onde de calibration
            arduinoController.updatePrintParameters(totalPoints, estimatedDurationSec);

            // 🔥 Récupérer la vitesse mise à jour
            double paperSpeedMmPerSec = arduinoController.getPaperSpeed();
            if (paperSpeedMmPerSec <= 0) {
                System.err.println("Invalid paper speed: " + paperSpeedMmPerSec + " mm/s. Aborting calibration.");
                return;
            }

            paperSpeedField.setText(String.format("%.2f mm/s", arduinoController.getPaperSpeed()));

            // Générer l'onde sinusoïdale
            List<Double> testWave = AutoCalibrateTest.generateCalibrationWave(totalPoints, 2.5, 1.0, 2.5);

            // Convertir l'onde en une liste de FrequencyData
            List<FrequencyData> dataPoints = testWave.stream()
                    .map(voltage -> new FrequencyData(0, voltage)) // Fréquence 0 car inutile ici
                    .toList();

            // Initialiser la barre de progression
            Platform.runLater(() -> {
                progressBar.setProgress(0);
                statusLabel.setText("Auto Calibration in Progress...");
            });

            updateCalculatedParameters(dataPoints);
            // 🔥 Lancer la transmission avec la bonne vitesse
            new Thread(() -> {
                arduinoController.startDataTransmission(dataPoints, paperSpeedMmPerSec);

                Platform.runLater(() -> {
                    statusLabel.setText("Calibration Complete.");
                    progressBar.setProgress(1.0);
                });

            }).start();

            System.out.println("Auto Calibration completed.");
        } else {
            System.err.println("Cannot calibrate: Arduino is not connected.");
        }
    }

    @FXML
    public void onAbout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dlraudio/ui/about.fxml"));
            Parent root = loader.load();

            Stage aboutStage = new Stage();
            aboutStage.getIcons().add(new Image(getClass().getResourceAsStream("/com/dlraudio/ui/images/logo.png")));
            aboutStage.setResizable(false);
            aboutStage.setTitle("About this fucking good app");
            aboutStage.setScene(new Scene(root));
            aboutStage.initModality(Modality.APPLICATION_MODAL);  // Empêche de cliquer sur la fenêtre principale
            aboutStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}