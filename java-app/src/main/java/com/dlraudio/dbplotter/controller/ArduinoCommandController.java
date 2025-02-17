package com.dlraudio.dbplotter.controller;

import com.dlraudio.dbplotter.model.FrequencyData;
import com.dlraudio.dbplotter.service.PrintSpeedCalculatorService;
import com.dlraudio.dbplotter.util.SerialPortUtils;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class ArduinoCommandController {

    private Consumer<Double> remainingTimeListener;
    private Consumer<Double> progressListener;
    private volatile boolean isTransmitting = false;

    public void setProgressListener(Consumer<Double> listener) {
        this.progressListener = listener;
    }

    public void setRemainingTimeListener(Consumer<Double> listener) {
        this.remainingTimeListener = listener;
    }

    private void updateRemainingTime(int pointsLeft, double timePerPointMs) {
        double remainingTimeSec = (pointsLeft * timePerPointMs) / 1000.0;
        if (remainingTimeListener != null) {
            remainingTimeListener.accept(remainingTimeSec);
        }
    }

    /**
     * Arrête immédiatement la transmission des données.
     */
    public void stopTransmission() {
        isTransmitting = false;
        sendCommand("STOP");
        System.out.println("Data transmission stopped.");
    }

    /**
     * Envoie une commande série à l'Arduino.
     */
    void sendCommand(String command) {
        if (SerialPortUtils.isConnected()) {
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
        if (!SerialPortUtils.isConnected() || dataPoints == null || dataPoints.isEmpty()) {
            System.err.println("No data to send or Arduino not connected.");
            return;
        }

        if (paperSpeedMmPerSec <= 0) {
            System.err.println("Invalid paper speed: " + paperSpeedMmPerSec + " mm/s. Aborting transmission.");
            return;
        }

        isTransmitting = true;
        double timePerPointMs = PrintSpeedCalculatorService.calculateTimePerPoint(paperSpeedMmPerSec);
        System.out.println("Time per point: " + timePerPointMs + " ms");

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<?> future = executor.submit(() -> {
            int totalPoints = dataPoints.size();
            for (int i = 0; i < totalPoints; i++) {
                if (!isTransmitting) {
                    System.out.println("Transmission aborted.");
                    break;
                }

                updateRemainingTime(totalPoints - i, timePerPointMs);
                double voltage = mapToVoltage(dataPoints.get(i).getMagnitude());
                sendCommand(String.format("DATA %.2f", voltage));

                if (progressListener != null) {
                    progressListener.accept((double) (i + 1) / totalPoints);
                }

                try {
                    Thread.sleep((long) timePerPointMs);
                } catch (InterruptedException e) {
                    System.out.println("Transmission interrupted.");
                    break;
                }
            }

            if (isTransmitting && progressListener != null) {
                progressListener.accept(1.0);
            }

            System.out.println("All data points sent.");
        });

        executor.shutdown();

        try {
            future.get(); // Bloque jusqu'à la fin de la tâche
        } catch (Exception e) {
            System.err.println("Error during transmission: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Transmission complete.");
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

    public void paperPush() {
        sendPwmFrequency(10);
        startMotor();
    }

    public void emergencyStop() {
        stopMotor();
        stopTransmission();
    }

    public boolean isTransmissionOngoing() {
        return isTransmitting;
    }
}
