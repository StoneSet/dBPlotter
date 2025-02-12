package com.dlraudio.dbplotter.service;

import com.dlraudio.dbplotter.model.FrequencyData;

import java.util.List;

public class PrintSpeedCalculatorService {

    public static final double DISTANCE_PER_POINT_MM = 0.1; // Distance en mm entre deux points imprimés

    /**
     * Calcule la durée totale du balayage en se basant sur l'écart de fréquences.
     * @param dataPoints Liste des points de fréquence
     * @return Durée estimée en secondes
     */
    public static double getTotalDuration(List<FrequencyData> dataPoints) {
        if (dataPoints == null || dataPoints.isEmpty()) {
            System.err.println("Invalid data points for duration calculation.");
            return 0.0;
        }
        double minFreq = FrequencyData.getMinFrequency(dataPoints);
        double maxFreq = FrequencyData.getMaxFrequency(dataPoints);

        // Facteur correctif basé sur la dynamique du balayage
        double duration = (maxFreq - minFreq) * 0.1;
        System.out.println("totalDurationSec: " + duration);
        return Math.max(duration, 10.0); // Minimum de 10s pour éviter une vitesse excessive
    }

    /**
     * Calcule la vitesse optimale du papier en mm/s.
     * @param numPoints Nombre total de points dans la mesure
     * @param durationSec Durée totale en secondes
     * @return Vitesse du papier en mm/s
     */
    public static double calculatePaperSpeed(int numPoints, double durationSec) {
        if (numPoints <= 0 || durationSec <= 0) {
            System.err.println("Invalid parameters for paper speed calculation.");
            return 0.0;
        }
        return (numPoints * DISTANCE_PER_POINT_MM) / durationSec;
    }

    /**
     * Calcule le temps entre chaque point en ms en fonction de la vitesse du papier.
     * @param paperSpeedMmPerSec Vitesse du papier en mm/s
     * @return Temps entre chaque point en ms
     */
    public static double calculateTimePerPoint(double paperSpeedMmPerSec) {
        if (paperSpeedMmPerSec <= 0) {
            System.err.println("Invalid paper speed: " + paperSpeedMmPerSec + " mm/s.");
            return 0.0;
        }
        return (DISTANCE_PER_POINT_MM / paperSpeedMmPerSec) * 1000;
    }

}
