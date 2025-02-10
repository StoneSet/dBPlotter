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

    /**
     * Downsamples the data by taking every nth point.
     *
     * @param data List of FrequencyData points.
     * @param step Step size for downsampling.
     * @return Downsampled list of FrequencyData points.
     */
    public static List<FrequencyData> downSampleData(List<FrequencyData> data, int step) {
        return data.stream()
                .filter(point -> data.indexOf(point) % step == 0)
                .collect(Collectors.toList());
    }

    /**
     * Smooths the data using a moving average.
     *
     * @param data       List of FrequencyData points.
     * @param windowSize Size of the window for the moving average.
     * @return Smoothed list of FrequencyData points.
     */
    public static List<FrequencyData> smoothData(List<FrequencyData> data, int windowSize) {
        if (data == null || data.size() < windowSize || windowSize <= 1) {
            return data;  // Not enough data or invalid window size
        }

        List<FrequencyData> smoothedData = new ArrayList<>();
        int halfWindow = windowSize / 2;

        for (int i = halfWindow; i < data.size() - halfWindow; i++) {
            double sumFrequency = 0.0;
            double sumMagnitude = 0.0;

            // Calculate average over the window
            for (int j = -halfWindow; j <= halfWindow; j++) {
                sumFrequency += data.get(i + j).getFrequency();
                sumMagnitude += data.get(i + j).getMagnitude();
            }

            double smoothedFrequency = sumFrequency / windowSize;
            double smoothedMagnitude = sumMagnitude / windowSize;

            smoothedData.add(new FrequencyData(smoothedFrequency, smoothedMagnitude));
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
