package com.dlraudio.dbplotter.service;

import com.dlraudio.dbplotter.model.FrequencyData;

import java.util.ArrayList;
import java.util.List;

public class LogScaleConverterService {

    public static final double PRINTABLE_WIDTH_MM = 150.0; // Largeur imprimable
    public static final double PRINTABLE_HEIGHT_MM = 50.0; // Hauteur imprimable
    public static final double FREQ_MIN_HZ = 20.0;
    public static final double FREQ_MAX_HZ = 20000.0;
    public static int N_POINTS = 1000; // Résolution par défaut
    public static double DEFAULT_PRINT_DURATION_SEC = 20; // Temps total cible par défaut
    // Calcul direct de la vitesse basée sur la durée cible
    public static final double FIXED_PAPER_SPEED = PRINTABLE_WIDTH_MM / DEFAULT_PRINT_DURATION_SEC;


    /**
     * Génère N fréquences réparties logarithmiquement entre 20Hz et 20kHz.
     */
    public static List<Double> generateLogSpacedFrequencies() {
        List<Double> frequencies = new ArrayList<>();
        double minLog = Math.log10(FREQ_MIN_HZ);
        double maxLog = Math.log10(FREQ_MAX_HZ);

        for (int i = 0; i < N_POINTS; i++) {
            double logFreq = minLog + (i / (double) (N_POINTS - 1)) * (maxLog - minLog);
            frequencies.add(Math.pow(10, logFreq));
        }
        return frequencies;
    }

    /**
     * Interpole les données brutes pour obtenir une liste de données interpolées.
     */
    public static List<FrequencyData> interpolateData(List<FrequencyData> rawData, List<Double> targetFrequencies) {
        List<FrequencyData> interpolatedData = new ArrayList<>();

        for (double freq : targetFrequencies) {
            double magnitude = interpolateMagnitude(rawData, freq);
            interpolatedData.add(new FrequencyData(freq, magnitude));
        }

        return interpolatedData;
    }

    /**
     * Interpole la magnitude pour une fréquence cible donnée.
     */
    private static double interpolateMagnitude(List<FrequencyData> data, double targetFreq) {
        FrequencyData lower = null, upper = null;

        for (FrequencyData point : data) {
            if (point.getFrequency() <= targetFreq) {
                lower = point;
            }
            if (point.getFrequency() >= targetFreq) {
                upper = point;
                break;
            }
        }

        if (lower == null) return upper != null ? upper.getMagnitude() : 0;
        if (upper == null) return lower.getMagnitude();

        double freqRatio = (targetFreq - lower.getFrequency()) / (upper.getFrequency() - lower.getFrequency());
        return lower.getMagnitude() + freqRatio * (upper.getMagnitude() - lower.getMagnitude());
    }

    /**
     * Calcule le temps par point pour une durée d'impression cible.
     */
    public static double calculateDefaultTimePerPoint() {
        return (DEFAULT_PRINT_DURATION_SEC * 1000) / N_POINTS;
    }
}
