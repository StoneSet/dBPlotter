package com.dlraudio.dbplotter.service;

import com.dlraudio.dbplotter.model.FrequencyData;
import com.dlraudio.dbplotter.util.SerialPortUtils;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class InterfaceCommandService {

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

        if (!startMotor(paperSpeedMmPerSec)) {
            System.err.println("Motor failed to start. Aborting transmission.");
            isTransmitting = false;
            return;
        }

        Future<?> future = executor.submit(() -> {
            int totalPoints = dataPoints.size();
            for (int i = 0; i < totalPoints; i++) {
                if (!isTransmitting) {
                    System.out.println("Transmission aborted.");
                    break;
                }

                updateRemainingTime(totalPoints - i, timePerPointMs);
                double voltage = Math.abs(mapToVoltage(dataPoints.get(i).getMagnitude()));
                String formattedVoltage = String.format(Locale.US, "DATA %.2f", voltage);

                SerialPortUtils.writeToPort(formattedVoltage);

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
            future.get();
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
     * @return true si le moteur a bien démarré, false sinon.
     */
    public boolean startMotor(double paperSpeedMmPerSec) {
        if (paperSpeedMmPerSec < 0.01 || paperSpeedMmPerSec > 30) {
            System.err.println("[ERROR] Vitesse invalide !");
            return false;
        }

        //TODO a revoir
        double frequency = Math.min(paperSpeedMmPerSec * 10, 350);
        String formattedFrequency = String.format(Locale.US, "%.2f", frequency);

        System.out.println("[CMD] Envoi de START_MOTOR à " + formattedFrequency + " Hz");
        return SerialPortUtils.sendCommandAndWaitForAck("START_MOTOR " + formattedFrequency, "MOTOR_STARTED", 5000).join();
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
     * Arrête le moteur et attend la confirmation "MOTOR_STOPPED".
     * @return true si le moteur s'est arrêté, false sinon.
     */
    public boolean stopMotor() {
        System.out.println("[CMD] Envoi de STOP_MOTOR");
        return SerialPortUtils.sendCommandAndWaitForAck("STOP_MOTOR", "MOTOR_STOPPED", 5000).join();
    }


    /**
     * Arrête immédiatement le moteur et la transmission des données.
     */
    public void emergencyStop() {
        stopTransmission();
        stopMotor();
    }

    /**
     * Arrête immédiatement la transmission des données.
     */
    public void stopTransmission() {
        isTransmitting = false;
        SerialPortUtils.writeToPort("STOP");
        System.out.println("Data transmission stopped.");
    }

    /**
     * Vérifie si une transmission est en cours.
     */
    public boolean isTransmissionOngoing() {
        return isTransmitting;
    }
}
