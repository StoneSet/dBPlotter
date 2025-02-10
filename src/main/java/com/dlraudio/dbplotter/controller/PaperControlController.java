package com.dlraudio.dbplotter.controller;

public class PaperControlController {

    private double distancePerPointMm; // Distance parcourue en mm par point
    private int totalPoints;           // Nombre total de points
    private double totalDurationSec;   // Durée totale en secondes

    public PaperControlController() {
        this.distancePerPointMm = 0.1;  // Exemple de distance parcourue par point en mm
    }

    /**
     * Configure les paramètres en fonction des données importées.
     */
    public void setupParameters(int pointsCount, double durationSec) {
        this.totalPoints = pointsCount;
        this.totalDurationSec = durationSec;
    }

    /**
     * Calcule la vitesse du papier en mm/s.
     */
    public double calculatePaperSpeed() {
        if (totalPoints > 0 && totalDurationSec > 0) {
            return (totalPoints * distancePerPointMm) / totalDurationSec;
        } else {
            return 0.0;  // Retourne 0 si les paramètres sont incorrects
        }
    }

    /**
     * Commande d’envoi de vitesse à l’Arduino.
     */
    public void sendSpeedToArduino() {
        double speed = calculatePaperSpeed();
        String command = String.format("SET_SPEED %.2f", speed);
        System.out.println("Sending speed command to Arduino: " + command);
        // Envoyer la commande via la communication série
    }

    public double getPaperSpeed() {
        return calculatePaperSpeed();
    }
}
