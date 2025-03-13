package com.dlraudio.dbplotter.test;

import java.util.ArrayList;
import java.util.List;

public class AutoCalibrateTest {

    /**
     * Génère une onde sinusoïdale pour l'auto-calibration du DAC.
     * @param samples Nombre de points de l'onde (résolution)
     * @param amplitude Amplitude max de la sinusoïde (en Volts)
     * @return Liste des tensions à envoyer au DAC
     */
    public static List<Double> generateCalibrationWave(int samples, double amplitude) {
        List<Double> wave = new ArrayList<>();

        for (int i = 0; i < samples; i++) {
            double angle = 2 * Math.PI * i / samples; // Angle en radians
            double voltage = (amplitude * Math.sin(angle)) + amplitude; // Oscille entre 0V et 5V
            wave.add(voltage);
        }

        return wave;
    }

}
