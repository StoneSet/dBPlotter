package com.dlraudio.dbplotter.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FrequencyData {
    private double frequency;
    private double magnitude;

    public FrequencyData(double frequency, double magnitude) {
        this.frequency = frequency;
        this.magnitude = magnitude;
    }

    // Getters
    public double getFrequency() {
        return frequency;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public static double getTotalDuration(List<FrequencyData> dataPoints) {
        if (dataPoints == null || dataPoints.isEmpty()) {
            return 0.0;
        }
        double minFreq = getMinFrequency(dataPoints);
        double maxFreq = getMaxFrequency(dataPoints);

        // Durée estimée basée sur l'écart de fréquences
        double duration = (maxFreq - minFreq) * 0.1; // FACTEUR À ADAPTER SI BESOIN
        return Math.max(duration, 10.0); // Assurer un minimum de 10s
    }


    //info here : https://dsp.stackexchange.com/questions/9967/1-n-octave-smoothing
    public static List<FrequencyData> smoothByOctave(List<FrequencyData> data, double octaveFraction) {
        if (data == null || data.isEmpty() || octaveFraction <= 0) {
            return data;
        }

        List<FrequencyData> smoothedData = new ArrayList<>();
        double bandwidthFactor = 0.6 * (1.0 / octaveFraction);  // Facteur ajusté pour le filtre

        for (int i = 0; i < data.size(); i++) {
            FrequencyData point = data.get(i);
            double centerFreq = point.getFrequency();

            // Limiter les points voisins dans une plage raisonnable
            double lowerBound = centerFreq / Math.pow(2, 1.0 / (2 * octaveFraction));
            double upperBound = centerFreq * Math.pow(2, 1.0 / (2 * octaveFraction));

            double sumWeights = 0;
            double sumWeightedFreq = 0;
            double sumWeightedMagnitude = 0;

            for (FrequencyData otherPoint : data) {
                double otherFreq = otherPoint.getFrequency();

                // Ignorer les points trop éloignés de la plage d'intérêt
                if (otherFreq < lowerBound || otherFreq > upperBound) continue;

                double weight = Math.exp(-Math.pow(Math.log10(otherFreq / centerFreq) / bandwidthFactor, 2));
                sumWeights += weight;
                sumWeightedFreq += weight * otherFreq;
                sumWeightedMagnitude += weight * otherPoint.getMagnitude();
            }

            if (sumWeights > 0) {
                smoothedData.add(new FrequencyData(sumWeightedFreq / sumWeights, sumWeightedMagnitude / sumWeights));
            } else {
                smoothedData.add(point);  // Garder le point original si aucun voisin trouvé
            }
        }

        return smoothedData;
    }



    // Methods for calculated parameters
    public static double getMinFrequency(List<FrequencyData> data) {
        return data.stream().mapToDouble(FrequencyData::getFrequency).min().orElse(0);
    }

    public static double getMaxFrequency(List<FrequencyData> data) {
        return data.stream().mapToDouble(FrequencyData::getFrequency).max().orElse(0);
    }

    public static double getMinMagnitude(List<FrequencyData> data) {
        return data.stream().mapToDouble(FrequencyData::getMagnitude).min().orElse(0);
    }

    public static double getMaxMagnitude(List<FrequencyData> data) {
        return data.stream().mapToDouble(FrequencyData::getMagnitude).max().orElse(0);
    }

    @Override
    public String toString() {
        return "FrequencyData{frequency=" + frequency + ", magnitude=" + magnitude + "}";
    }
}
