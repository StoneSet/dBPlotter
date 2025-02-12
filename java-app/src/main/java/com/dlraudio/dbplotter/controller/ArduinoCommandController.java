package com.dlraudio.dbplotter.controller;

import com.dlraudio.dbplotter.model.FrequencyData;
import com.dlraudio.dbplotter.service.PrintSpeedCalculatorService;
import com.dlraudio.dbplotter.util.SerialPortUtils;
import java.util.List;
import java.util.function.Consumer;

public class ArduinoCommandController {

    private static final int BAUD_RATE = 115200;
    private String currentPort;
    private Consumer<Double> progressListener;
    private volatile boolean isTransmitting = false;

    public void setProgressListener(Consumer<Double> listener) {
        this.progressListener = listener;
    }

    public boolean isConnected() {
        return true;
        //test purpose
        //return SerialPortUtils.isConnected();
    }

    /**
     * Arrête immédiatement la transmission des données.
     */
    public void stopTransmission() {
        isTransmitting = false;
        sendCommand("stopMotor");
        sendCommand("STOP");
        System.out.println("Data transmission stopped.");
    }

    public ArduinoCommandController() {}

    /**
     * Configure le port COM et établit la connexion.
     */
    public void setupPort(String port) {
        this.currentPort = port;
        SerialPortUtils.connect(port, BAUD_RATE);
    }

    /**
     * Envoie une commande série à l'Arduino.
     */
    void sendCommand(String command) {
        if (isConnected() && currentPort != null) {
            SerialPortUtils.writeToPort(command);
            System.out.println("Sent to Arduino: " + command);
        } else {
            System.err.println("Cannot send command. Arduino is not connected.");
        }
    }

    /**
     * Envoie les données du graphique à l'Arduino et convertit en tension DAC.
     */
    public void startDataTransmission(List<FrequencyData> dataPoints, double paperSpeedMmPerSec) {
        if (!isConnected() || dataPoints == null || dataPoints.isEmpty()) {
            System.err.println("No data to send or Arduino not connected.");
            return;
        }

        if (paperSpeedMmPerSec <= 0) {
            System.err.println("Invalid paper speed: " + paperSpeedMmPerSec + " mm/s. Aborting transmission.");
            return;
        }

        isTransmitting = true;

        // Calcul du temps entre chaque point en ms
        double timePerPointMs = PrintSpeedCalculatorService.calculateTimePerPoint(paperSpeedMmPerSec);
        System.out.println("Time per point: " + timePerPointMs + " ms");

        new Thread(() -> {
            int totalPoints = dataPoints.size();
            for (int i = 0; i < totalPoints; i++) {
                if (!isTransmitting) {
                    System.out.println("Transmission aborted.");
                    break;
                }

                double voltage = mapToVoltage(dataPoints.get(i).getMagnitude());
                sendCommand(String.format("DATA %.2f", voltage));

                // Notifier MainController via callback
                double progress = (double) (i + 1) / totalPoints;
                if (progressListener != null) {
                    progressListener.accept(progress);
                }

                // Pause ajustée dynamiquement
                try {
                    Thread.sleep((long) timePerPointMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (isTransmitting && progressListener != null) {
                progressListener.accept(1.0); // Transmission terminée
            }

            System.out.println("All data points sent.");
        }).start();
    }


    /**
     * Convertit les valeurs de dB en tension pour le DAC.
     */
    private double mapToVoltage(double magnitudeDb) {
        double minDb = -60.0;
        double maxDb = 0.0;
        double minVoltage = 0.0;
        double maxVoltage = 5.0;
        return minVoltage + (maxVoltage - minVoltage) * ((magnitudeDb - minDb) / (maxDb - minDb));
    }

    /**
     * Envoie une fréquence TTL au moteur.
     */
    public void sendPwmFrequency(int frequency) {
        sendCommand(String.format("TTL_FREQ %d", frequency));
    }

    /**
     * Commande pour démarrer le moteur d'impression.
     */
    public void startMotor() {
        sendCommand("START_MOTOR");
    }

    /**
     * Commande pour arrêter l'impression.
     */
    public void stopMotor() {

        sendCommand("STOP_MOTOR");
    }
}
