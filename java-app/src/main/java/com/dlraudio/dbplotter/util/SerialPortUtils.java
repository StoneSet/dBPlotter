package com.dlraudio.dbplotter.util;

import com.fazecast.jSerialComm.SerialPort;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

public class SerialPortUtils {

    private static SerialPort serialPort;
    private static Thread listenerThread;
    private static Consumer<String> messageListener;

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

    /**
     * Ajoute un listener qui sera appelé dès qu'un message est reçu.
     * TODO : faire le systeme d'attente de message de la part de l'arduino pour lancer une autre impression
     */
    public static void setMessageListener(Consumer<String> listener) {
        messageListener = listener;
    }

    /**
     * Écoute en continu les messages du port série.
     */
    private static void startListening() {
        if (listenerThread != null && listenerThread.isAlive()) {
            return;
        }

        listenerThread = new Thread(() -> {
            try {
                InputStream inputStream = serialPort.getInputStream();
                byte[] buffer = new byte[1024];

                while (serialPort.isOpen()) {
                    int numBytes = inputStream.read(buffer);
                    if (numBytes > 0) {
                        String received = new String(buffer, 0, numBytes).trim();
                        System.out.println("Received: " + received);

                        if (messageListener != null) {
                            messageListener.accept(received);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error in serial listening: " + e.getMessage());
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }
}
