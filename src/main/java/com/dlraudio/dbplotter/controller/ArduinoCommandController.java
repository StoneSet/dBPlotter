package com.dlraudio.dbplotter.controller;

import com.dlraudio.dbplotter.util.SerialPortUtils;

public class ArduinoCommandController {

    private static final int BAUD_RATE = 9600;
    private String currentPort;

    /**
     * Configure le port COM à utiliser.
     */
    public void setupPort(String port) {
        this.currentPort = port;
    }

    /**
     * Envoie une commande simple à l'Arduino.
     */
    public void sendCommand(String command) {
        if (currentPort != null && SerialPortUtils.isConnected()) {
            SerialPortUtils.writeToPort(command);
            System.out.println("Command sent to Arduino: " + command);
        } else {
            System.err.println("Cannot send command. No connection available.");
        }
    }

    /**
     * Envoie une commande pour régler la vitesse du papier.
     */
    public void setPaperSpeed(double speedMmPerSec) {
        String command = String.format("SET_SPEED %.2f", speedMmPerSec);
        sendCommand(command);
    }

    /**
     * Envoie les données CSV point par point.
     */
    public void sendDataPoint(double frequency, double magnitude) {
        String command = String.format("DATA_POINT %.2f %.2f", frequency, magnitude);
        sendCommand(command);
    }

    /**
     * Commande pour démarrer ou arrêter la machine.
     */
    public void startMachine() {
        sendCommand("START_MACHINE");
    }

    public void stopMachine() {
        sendCommand("STOP_MACHINE");
    }

    /**
     * Déconnecte l'Arduino.
     */
    public void disconnectArduino() {
        SerialPortUtils.disconnect();
        System.out.println("Arduino disconnected.");
    }
}
