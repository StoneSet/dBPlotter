package com.dlraudio.dbplotter.util;

import com.fazecast.jSerialComm.SerialPort;

import java.io.InputStream;
import java.io.OutputStream;

public class SerialPortUtils {

    private static SerialPort serialPort;

    // Lister les ports disponibles
    public static String[] listAvailablePorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        String[] portNames = new String[ports.length];
        for (int i = 0; i < ports.length; i++) {
            portNames[i] = ports[i].getSystemPortName();
        }
        return portNames;
    }

    // Connecter au port série
    public static boolean connect(String portName, int baudRate) {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setComPortParameters(baudRate, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 1000);

        if (serialPort.openPort()) {
            System.out.println("Connected to " + portName);
            return true;
        } else {
            System.err.println("Failed to connect to " + portName);
            return false;
        }
    }

    // Déconnecter du port série
    public static void disconnect() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("Disconnected.");
        }
    }

    // Envoyer des données au port série (writeToPort)
    public static void writeToPort(String data) {
        if (serialPort != null && serialPort.isOpen()) {
            try {
                OutputStream outputStream = serialPort.getOutputStream();
                outputStream.write(data.getBytes());
                outputStream.flush();
                System.out.println("Data sent: " + data);
            } catch (Exception e) {
                System.err.println("Error sending data: " + e.getMessage());
            }
        } else {
            System.err.println("Port not open.");
        }
    }

    // Vérifier si le port est connecté (isConnected)
    public static boolean isConnected() {
        return serialPort != null && serialPort.isOpen();
    }

    // Lire les données du port série
    public static String readData() {
        if (serialPort != null && serialPort.isOpen()) {
            try {
                InputStream inputStream = serialPort.getInputStream();
                byte[] buffer = new byte[1024];
                int numBytes = inputStream.read(buffer);
                return new String(buffer, 0, numBytes);
            } catch (Exception e) {
                System.err.println("Error reading data: " + e.getMessage());
            }
        }
        return null;
    }
}
