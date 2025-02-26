package com.dlraudio.dbplotter.controller;

import com.dlraudio.dbplotter.MainApp;
import com.dlraudio.dbplotter.model.FrequencyData;
import com.dlraudio.dbplotter.model.PlotParameters;
import com.dlraudio.dbplotter.service.PlottingService;
import com.dlraudio.dbplotter.service.PrintSpeedCalculatorService;
import com.dlraudio.dbplotter.test.AutoCalibrateTest;
import com.dlraudio.dbplotter.util.CsvImporter;
import com.dlraudio.dbplotter.util.FileUtils;
import com.dlraudio.dbplotter.util.SerialPortUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainController {

    @FXML
    public MenuItem smoothingNoneMenuItem;
    @FXML
    public Label currentFileLabel;
    public Label remainingTimeLabel;
    @FXML
    private Menu selectComPortMenu;
    @FXML
    private ImageView dacStatusIcon;
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
    public ImageView txActivity;
    @FXML
    public ImageView rxActivity;
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
    private PlottingService plottingService;
    private PlotParameters plotParameters;
    private double importedPaperSpeedMmPerSec = 0.0;
    private final ArduinoCommandController arduinoController = new ArduinoCommandController();

    private static MainController instance;

    public MainController() {
        instance = this;
    }

    public static MainController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        disconnectMenuItem.setDisable(true);
        disableSmoothingMenus(true);
        updateButtonStates();
        connectMenuItem.setDisable(true);

        plottingService = new PlottingService(lineChart);
        plottingService.initializePlot("Frequency (Hz)", "Amplitude (dB)");

        arduinoController.setProgressListener(progress ->
                Platform.runLater(() -> progressBar.setProgress(progress))
        );

        arduinoController.setRemainingTimeListener(this::updateRemainingTimeLabel);
    }

    /*
     * Mise à jour de l'indicateur d'activité de transmission. (que ça soit TX ou RX)
     */
    public void blinkIndicator(ImageView indicator) {
        Platform.runLater(() -> {
            indicator.setImage(new Image(getClass().getResourceAsStream("/com/dlraudio/ui/images/green_light.png")));
        });

        // repasser en rouge après 10 ms
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
            Platform.runLater(() -> {
                indicator.setImage(new Image(getClass().getResourceAsStream("/com/dlraudio/ui/images/red_light.png")));
            });
        }).start();
    }


    /*
     * Mise à jour de l'étiquette de temps restant.
     */
    private void updateRemainingTimeLabel(double remainingTimeSec) {
        Platform.runLater(() ->
                remainingTimeLabel.setText(String.format("%.2f seconds", remainingTimeSec))
        );
    }

    /*
     * Mise à jour de l'état des boutons en fonction de la connexion à l'Arduino et de l'importation du fichier CSV.
     */
    private void updateButtonStates() {
        boolean enableArduinoActions = SerialPortUtils.isConnected();

        boolean enableSendTo2306 = enableArduinoActions && isCsvImported;

        sendTo2306Button.setDisable(!enableSendTo2306);
        stopButton.setDisable(true);
        paperPushButton.setDisable(!enableArduinoActions);
        autoCalibrateButton.setDisable(!enableArduinoActions);

        connectMenuItem.setDisable(enableArduinoActions);
        disconnectMenuItem.setDisable(!enableArduinoActions);
    }


    /*
     * Désactiver les menus de lissage en fonction de l'état de l'importation du fichier CSV.
     */
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

    /*
     * Mise à jour de l'icône de l'état de connexion DAC.
     */
    private void updateDacStatusIcon(boolean isConnected) {
        String imagePath = isConnected ? "/com/dlraudio/ui/images/pc_connected.png"
                : "/com/dlraudio/ui/images/pc_disconnected.png";
        dacStatusIcon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
    }

    /*
     * Mise à jour des paramètres calculés à partir des données brutes.
     */
    private void updateCalculatedParameters(List<FrequencyData> dataPoints) {
        double minFreq = FrequencyData.getMinFrequency(dataPoints);
        double maxFreq = FrequencyData.getMaxFrequency(dataPoints);
        double minDb = FrequencyData.getMinMagnitude(dataPoints);
        double maxDb = FrequencyData.getMaxMagnitude(dataPoints);

        plotParameters = new PlotParameters(minFreq, maxFreq, minDb, maxDb);
        Platform.runLater(this::displayCalculatedParameters);
    }

    /*
     * Affichage des paramètres calculés dans les étiquettes.
     */
    private void displayCalculatedParameters() {
        if (plotParameters != null) {
            minFrequencyField.setText(String.format("%.2f Hz", plotParameters.getMinFrequency()));
            maxFrequencyField.setText(String.format("%.2f Hz", plotParameters.getMaxFrequency()));
            minDbField.setText(String.format("%.2f dB", plotParameters.getMinDb()));
            maxDbField.setText(String.format("%.2f dB", plotParameters.getMaxDb()));
        }
    }

    /*
     * Désactiver les boutons pendant l'exécution de tâches.
     */
    private void setButtonsDisabled(boolean running) {
        //System.out.println("Setting buttons disabled: " + running);
        Platform.runLater(() -> {
            sendTo2306Button.setDisable(!isCsvImported || running);
            paperPushButton.setDisable(running);
            autoCalibrateButton.setDisable(running);
            stopButton.setDisable(!running); // ✅ STOP doit être activé quand les autres sont désactivés
        });
    }

    /*
     * Affichage du menu de sélection du port COM.
     */
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
                MenuItem portItem = getMenuItem(port);

                selectComPortMenu.getItems().add(portItem);
            }
        }
    }

    /*
     * Créer un élément de menu pour le port COM. L'élément est désactivé si le port est déjà connecté.
     */
    private MenuItem getMenuItem(String port) {
        MenuItem portItem = new MenuItem(port);
        portItem.setOnAction(event -> onPortSelected(port));

        if (SerialPortUtils.isConnected() && port.equals(selectedPort)) {
            portItem.setDisable(false);
            portItem.setStyle("-fx-font-weight: bold; -fx-text-fill: blue;");
        } else {
            portItem.setDisable(SerialPortUtils.isConnected());
        }
        return portItem;
    }

    private void onPortSelected(String port) {
        if (SerialPortUtils.isConnected()) {
            System.out.println("Cannot change port while connected.");
            return;
        }

        selectedPort = port;
        portLabel.setText(port);
        connectMenuItem.setDisable(false);
        System.out.println("Selected port: " + port);
    }

    @FXML
    public void onConnect() {
        if (SerialPortUtils.isConnected()) {
            System.out.println("Already connected to " + selectedPort);
            return;
        }

        if (selectedPort != null && SerialPortUtils.connect(selectedPort)) {
            statusLabel.setText("Connected");
            updateDacStatusIcon(true);
            System.out.println("Connected to " + selectedPort);
            onSelectComPortMenuShown();
        } else {
            statusLabel.setText("Failed to connect.");
            System.err.println("Connection failed.");
        }
        updateButtonStates();
    }

    @FXML
    public void onDisconnect() {
        SerialPortUtils.disconnect();
        updateButtonStates();
        updateDacStatusIcon(false);
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

            if (type.equalsIgnoreCase("rew")) {
                dataPoints = csvImportService.importFromRew(csvFile);
            } else {
                dataPoints = csvImportService.importFromArta(csvFile);
            }

            if (dataPoints != null && !dataPoints.isEmpty()) {
                plottingService.plotData(dataPoints);

                updateCalculatedParameters(dataPoints);
                double estimatedDurationSec = PrintSpeedCalculatorService.getTotalDuration(dataPoints);
                importedPaperSpeedMmPerSec = PrintSpeedCalculatorService.calculatePaperSpeed(dataPoints.size(), estimatedDurationSec);

                paperSpeedField.setText(String.format("%.2f mm/s", importedPaperSpeedMmPerSec));

                disableSmoothingMenus(false);

                currentFileLabel.setText(csvFile.getName());
                isCsvImported = true;
                updateButtonStates();

                statusLabel.setText("CSV data imported and plotted.");

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
        if (SerialPortUtils.isConnected()) {
            List<FrequencyData> dataPoints = plottingService.getCurrentData();
            if (dataPoints == null || dataPoints.isEmpty()) {
                System.err.println("No data available for transmission.");
                return;
            }

            double paperSpeedToUse = importedPaperSpeedMmPerSec;

            if (paperSpeedToUse <= 0) {
                System.err.println("Invalid paper speed: " + paperSpeedToUse + " mm/s. Cannot proceed.");
                return;
            }

            Platform.runLater(() -> {
                setButtonsDisabled(true);
                statusLabel.setText("Data transmission in progress.");
            });

            Task<Void> sendTask = new Task<>() {
                @Override
                protected Void call() {
                    arduinoController.startDataTransmission(dataPoints, paperSpeedToUse);
                    return null;
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> setButtonsDisabled(false));
                    statusLabel.setText("Data transmission complete.");
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> setButtonsDisabled(false));
                    statusLabel.setText("Transmission failed.");
                }
            };

            new Thread(sendTask).start();
        }
    }

    private Window getWindow() {
        return statusLabel.getScene().getWindow();
    }

    @FXML
    public void onExit() {
        MainApp.closeApplication();
    }

    @FXML
    private void onStop() {
        if (SerialPortUtils.isConnected()) {
            Platform.runLater(() -> setButtonsDisabled(true));

            Task<Void> stopTask = new Task<>() {
                @Override
                protected Void call() {
                    arduinoController.emergencyStop();
                    return null;
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        setButtonsDisabled(false);
                        statusLabel.setText("Machine stopped.");
                    });
                    System.out.println("Stop command sent to Arduino.");
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        setButtonsDisabled(false);
                        statusLabel.setText("Stop command failed.");
                    });
                    System.err.println("Failed to send stop command.");
                }
            };
            new Thread(stopTask).start();
        } else {
            System.err.println("Cannot stop: Arduino is not connected.");
        }
    }

    @FXML
    private void onPaperPush() {
        if (SerialPortUtils.isConnected()) {
            Platform.runLater(() -> {
                setButtonsDisabled(true);
                statusLabel.setText("Paper advancing...");
            });

            Task<Void> paperPushTask = new Task<>() {
                @Override
                protected Void call() {
                    arduinoController.paperPush(1, 2000);
                    return null;
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> setButtonsDisabled(false));
                    statusLabel.setText("Paper push completed.");
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> setButtonsDisabled(false));
                    statusLabel.setText("Paper push failed.");
                }
            };

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(paperPushTask);
            executor.shutdown();
        } else {
            System.err.println("Cannot push paper: Arduino is not connected.");
        }
    }

    @FXML
    private void onAutoCalibrate() {
        if (SerialPortUtils.isConnected()) {

            Platform.runLater(() -> {
                setButtonsDisabled(true);
                statusLabel.setText("Auto Calibrating...");
            });

            Task<Void> autoCalibrateTask = new Task<>() {
                @Override
                protected Void call() {
                    int totalPoints = 100;
                    double estimatedDurationSec = 10.0;

                    List<Double> testWave = AutoCalibrateTest.generateCalibrationWave(totalPoints, 2.5, 2.5);
                    List<FrequencyData> dataPoints = testWave.stream()
                            .map(voltage -> new FrequencyData(0, voltage))
                            .toList();

                    Platform.runLater(() -> {
                        progressBar.setProgress(0);
                    });

                    double calibrationSpeed = PrintSpeedCalculatorService.calculatePaperSpeed(totalPoints, estimatedDurationSec);
                    arduinoController.startDataTransmission(dataPoints, calibrationSpeed);

                    return null;
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> setButtonsDisabled(false));
                    statusLabel.setText("Calibration Complete.");
                    progressBar.setProgress(1.0);
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> setButtonsDisabled(false));
                    statusLabel.setText("Calibration failed.");
                }
            };

            new Thread(autoCalibrateTask).start();
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
            aboutStage.initModality(Modality.APPLICATION_MODAL);
            aboutStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}