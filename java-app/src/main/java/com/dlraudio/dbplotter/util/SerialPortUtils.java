package com.dlraudio.dbplotter.util;

import com.dlraudio.dbplotter.controller.MainController;
import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class SerialPortUtils {

    private static final int BAUD_RATE = 9600;
    private static final int DataBits = 8;
    private static final int StopBits = SerialPort.ONE_STOP_BIT;
    private static final int Parity   = SerialPort.NO_PARITY;

    private static SerialPort serialPort;
    private static Thread listenerThread;
    private static final List<Consumer<String>> listeners = new CopyOnWriteArrayList<>();
    private static final ConcurrentHashMap<String, CompletableFuture<Boolean>> ackFutures = new ConcurrentHashMap<>();

    private static ImageView txIndicator;
    private static ImageView rxIndicator;

    public static void setIndicators(ImageView tx, ImageView rx) {
        txIndicator = tx;
        rxIndicator = rx;
    }

    /**
     * Lister les ports série disponibles
     */
    public static String[] listAvailablePorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        String[] portNames = new String[ports.length];
        for (int i = 0; i < ports.length; i++) {
            portNames[i] = ports[i].getSystemPortName();
        }
        return portNames;
    }

    /**
     * Connecter au port série et attendre READY via le listener
     */
    public static boolean connect(String portName) {
        if (serialPort != null && serialPort.isOpen()) {
            LogUtils.log("Port already open: " + portName);
            return true;
        }

        serialPort = SerialPort.getCommPort(portName);
        serialPort.setComPortParameters(BAUD_RATE, DataBits, StopBits, Parity);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 1000);

        startListening();

        if (!serialPort.openPort()) {
            LogUtils.logError("Issue while connecting to " + portName);
            return false;
        }

        LogUtils.log("Connected to " + portName + ". Waiting READY...");

        CompletableFuture<Boolean> readyFuture = new CompletableFuture<>();
        addMessageListener(message -> {
            if ("READY".equalsIgnoreCase(message.trim())) {
                LogUtils.log("OK");
                readyFuture.complete(true);
            }
        });

        try {
            return readyFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            LogUtils.logError("Timeout: READY not received.");            disconnect();
            return false;
        }
    }

    /**
     * Déconnecter du port série
     */
    public static void disconnect() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            LogUtils.logError("Port isn't open.");        }
        ackFutures.clear();
    }

    /**
     * Vérifie si le port est connecté
     */
    public static boolean isConnected() {
        return serialPort != null && serialPort.isOpen();
    }

    /**
     * Envoie une commande et attend un ACK
     */
    public static CompletableFuture<Boolean> sendCommandAndWaitForAck(String command, String expectedAck, long timeoutMs) {
        if (serialPort == null || !serialPort.isOpen()) {
            LogUtils.logError("Port isn't open.");
            return CompletableFuture.completedFuture(false);
        }
        writeToPort(command);

        CompletableFuture<Boolean> ackFuture = new CompletableFuture<>();
        ackFutures.put(expectedAck, ackFuture);

        CompletableFuture.delayedExecutor(timeoutMs, TimeUnit.MILLISECONDS)
                .execute(() -> {
                    if (!ackFuture.isDone()) {
                        LogUtils.logError("Timeout: ACK " + expectedAck + " not received.");
                        ackFuture.complete(false);
                    }
                });

        return ackFuture;
    }

    /**
     * Envoie une commande sans attendre d'ACK
     */
    public static void writeToPort(String data) {
        if (serialPort != null && serialPort.isOpen()) {
            try {
                OutputStream outputStream = serialPort.getOutputStream();
                outputStream.write((data + "\n").getBytes());
                outputStream.flush();
                LogUtils.logTx(data);
                blinkIndicator(txIndicator);
            } catch (Exception e) {
                LogUtils.logError("Error while sending: " + e.getMessage());
            }
        } else {
            LogUtils.logError("Port is not open.");
        }
    }

    /**
     * Ajoute un listener pour les messages reçus.
     */
    public static void addMessageListener(Consumer<String> listener) {
        listeners.add(listener);
    }

    /**
     * Supprime un listener spécifique.
     */
    public static void removeMessageListener(Consumer<String> listener) {
        listeners.remove(listener);
    }

    /**
     * Démarre un thread unique pour écouter les messages série
     */
    private static void startListening() {
        if (listenerThread != null && listenerThread.isAlive()) return;

        listenerThread = new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

                while (serialPort != null && serialPort.isOpen()) {
                    if (!reader.ready()) continue;

                    String received = reader.readLine();
                    if (received != null && !received.trim().isEmpty()) {
                        LogUtils.logRx(received);
                        blinkIndicator(rxIndicator);

                        for (Consumer<String> listener : listeners) {
                            listener.accept(received);
                        }

                        CompletableFuture<Boolean> ackFuture = ackFutures.remove(received.trim());
                        if (ackFuture != null) {
                            ackFuture.complete(true);
                        }
                    }
                }
            } catch (Exception e) {
                LogUtils.logError("Error: " + e.getMessage());            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Fait clignoter un indicateur LED (RX ou TX)
     */
    private static void blinkIndicator(ImageView indicator) {
        if (indicator == null) return;

        Platform.runLater(() -> indicator.setImage(new Image(Objects.requireNonNull(MainController.class.getResourceAsStream("/com/dlraudio/ui/images/green_light.png")))));

        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
            Platform.runLater(() -> indicator.setImage(new Image(Objects.requireNonNull(MainController.class.getResourceAsStream("/com/dlraudio/ui/images/red_light.png")))));
        }).start();
    }
}
