package com.dlraudio.dbplotter.test;

import java.util.ArrayList;
import java.util.List;

public class AutoCalibrateTest {

    /**
     * Génère une onde sinusoïdale pour l'auto-calibration du DAC.
     * @param samples Nombre de points de l'onde
     * @param amplitude Amplitude max de la sinusoïde (en Volts)
     * @param frequency Fréquence de l'onde (Hz)
     * @param offset Décalage DC de l'onde (en Volts)
     * @return Liste des tensions à envoyer au DAC
     */
    public static List<Double> generateCalibrationWave(int samples, double amplitude, double frequency, double offset) {
        List<Double> wave = new ArrayList<>();

        for (int i = 0; i < samples; i++) {
            double angle = 2 * Math.PI * i / samples;
            wave.add(offset + amplitude * Math.sin(angle)); // Ajoute l'offset DC
        }

        return wave;
    }
}
