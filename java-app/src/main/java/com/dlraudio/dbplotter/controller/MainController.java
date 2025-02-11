package com.dlraudio.dbplotter.controller;

import com.dlraudio.dbplotter.model.FrequencyData;
import com.dlraudio.dbplotter.service.PlottingService;
import com.dlraudio.dbplotter.util.CsvImporter;
import com.dlraudio.dbplotter.util.FileUtils;
import com.dlraudio.dbplotter.util.SerialPortUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.stage.Window;

import java.io.File;
import java.util.List;

public class MainController {

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
    private Label paperSpeedField;

    @FXML
    private Label maxDbField;

    @FXML
    private Label portLabel;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private LineChart<Number, Number> lineChart;

    private String selectedPort;
    private boolean isConnected = false;

    private PlottingService plottingService;

    @FXML
    public void initialize() {
        connectMenuItem.setDisable(true);
        disconnectMenuItem.setDisable(true);

        plottingService = new PlottingService(lineChart);
        plottingService.initializePlot("Frequency (Hz)", "Amplitude (dB)");
    }

    private PaperControlController paperController = new PaperControlController();

    private void updateCalculatedParameters(List<FrequencyData> dataPoints) {
        double minFreq = FrequencyData.getMinFrequency(dataPoints);
        double maxFreq = FrequencyData.getMaxFrequency(dataPoints);
        double minDb = FrequencyData.getMinMagnitude(dataPoints);
        double maxDb = FrequencyData.getMaxMagnitude(dataPoints);

        minFrequencyField.setText(String.format("%.2f Hz", minFreq));
        maxFrequencyField.setText(String.format("%.2f Hz", maxFreq));
        minDbField.setText(String.format("%.2f dB", minDb));
        maxDbField.setText(String.format("%.2f dB", maxDb));

        // Configuration des paramètres de vitesse du papier
        paperController.setupParameters(dataPoints.size(), 60.0);  // Durée fictive : 60 secondes
        double paperSpeed = paperController.calculatePaperSpeed();

        // Mise à jour du champ de la vitesse du papier
        paperSpeedField.setText(String.format("%.2f mm/s", paperSpeed));
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
        if (selectedPort != null && SerialPortUtils.connect(selectedPort, 9600)) {
            arduinoController.setupPort(selectedPort);
            isConnected = true;
            statusLabel.setText("Connected");
            disconnectMenuItem.setDisable(false);
            connectMenuItem.setDisable(true);
            System.out.println("Connected to " + selectedPort);
        } else {
            statusLabel.setText("Failed to connect.");
            System.err.println("Connection failed.");
        }
    }

    @FXML
    public void onDisconnect() {
        SerialPortUtils.disconnect();
        isConnected = false;
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

            // Importation des données CSV en fonction du type
            if (type.equalsIgnoreCase("rew")) {
                dataPoints = csvImportService.importFromRew(csvFile);
            } else {
                dataPoints = csvImportService.importFromArta(csvFile);
            }

            // Vérification et traitement des données importées
            if (dataPoints != null && !dataPoints.isEmpty()) {
                // Échantillonnage et tracé des données
                List<FrequencyData> downsampledData = FrequencyData.downSampleData(dataPoints, 10);  // Exemple : 1 point tous les 10
                plottingService.plotData(downsampledData);

                // Mise à jour des paramètres calculés, y compris la vitesse du papier
                updateCalculatedParameters(dataPoints);

                statusLabel.setText("CSV data imported and plotted.");
            } else {
                statusLabel.setText("No data found in the CSV file.");
            }
        }
    }


    @FXML
    public void onSendTo2306() {
        System.out.println("Starting data transfer to 2306...");
        progressBar.setProgress(0.0);

        new Thread(() -> {
            int totalSteps = 100;
            for (int i = 1; i <= totalSteps; i++) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                double progress = (double) i / totalSteps;
                Platform.runLater(() -> progressBar.setProgress(progress));
            }

            Platform.runLater(() -> {
                System.out.println("Data transfer completed.");
                statusLabel.setText("Data transfer completed.");
            });
        }).start();
    }

    private Window getWindow() {
        return statusLabel.getScene().getWindow();
    }

    @FXML
    public void onLoadSetup() {
        System.out.println("Load Setup clicked");
    }

    @FXML
    public void onSaveSetup() {
        System.out.println("Save Setup clicked");
    }

    @FXML
    public void onSaveSetupAs() {
        System.out.println("Save Setup As clicked");
    }

    @FXML
    public void onExit() {
        System.exit(0);
    }

    @FXML
    public void onStop() {
        System.out.println("Stop clicked");
    }

    @FXML
    public void onPaperPush() {
        System.out.println("Paper Push clicked");
    }

    @FXML
    public void onAutoCalibrate() {
        System.out.println("Auto Calibrate clicked");
    }

    @FXML
    public void onAbout() {
        System.out.println("About clicked");
    }
}
