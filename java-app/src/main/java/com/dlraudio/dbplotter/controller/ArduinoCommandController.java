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

    /**
     * Définit un écouteur pour la progression de la transmission des données.
     */
    public void setProgressListener(Consumer<Double> listener) {
        this.progressListener = listener;
    }

    /**
     * Définit un écouteur pour le temps restant estimé de la transmission des données.
     */
    public void setRemainingTimeListener(Consumer<Double> listener) {
        this.remainingTimeListener = listener;
    }

    /**
     * Met à jour le temps restant estimé pour la transmission des données.
     */
    private void updateRemainingTime(int pointsLeft, double timePerPointMs) {
        double remainingTimeSec = (pointsLeft * timePerPointMs) / 1000.0;
        if (remainingTimeListener != null) {
            remainingTimeListener.accept(remainingTimeSec);
        }
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

        startMotor(paperSpeedMmPerSec);

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
        isTransmitting = false;
        stopMotor();
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
     * Démarre le moteur et configure immédiatement la vitesse en mm/s.
     * La vitesse est convertie en fréquence pour le moteur.
     * Vitesse minimale : 0.01 mm/s
     * Vitesse maximale : 30 mm/s
     * @param paperSpeedMmPerSec Vitesse du papier en mm/s
     *                           (1 mm/s correspond à 10 Hz pour le moteur)
     */
    public void startMotor(double paperSpeedMmPerSec) {
        if (paperSpeedMmPerSec < 0.01 || paperSpeedMmPerSec > 30) {
            System.err.println("Invalid paper speed: " + paperSpeedMmPerSec + " mm/s. Must be >= 0.01 and <= 30.");
            return;
        }

        // Conversion mm/s → Hz
        double frequency = paperSpeedMmPerSec * 10; // A VERIFIER !! Supposons que 1 mm/s correspond à 10 Hz

        frequency = Math.min(frequency, 350);

        String formattedFrequency = String.format("%.2f", frequency);

        sendCommand("START_MOTOR " + formattedFrequency);
        System.out.println("Start motor command sent at " + formattedFrequency + " Hz.");
    }

    /**
     * Commande pour pousser le papier sur un temps défini.
     */
    public void paperPush(double paperSpeed, int durationMs) {

        System.out.println("Pushing paper at " + paperSpeed + " mm/s for " + (durationMs / 1000.0) + " sec");

        startMotor(paperSpeed);

        new Thread(() -> {
            try {
                Thread.sleep(durationMs);
            } catch (InterruptedException e) {
                System.err.println("Paper push interrupted.");
            }
            stopMotor();
            System.out.println("Paper push complete.");
        }).start();
    }

    /**
     * Commande pour arrêter l'impression.
     */
    public void stopMotor() {
        sendCommand("STOP_MOTOR");
    }

    /**
     * Arrête immédiatement le moteur et la transmission des données.
     */
    public void emergencyStop() {
        stopMotor();
        stopTransmission();
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
     * Vérifie si une transmission est en cours.
     */
    public boolean isTransmissionOngoing() {
        return isTransmitting;
    }
}
