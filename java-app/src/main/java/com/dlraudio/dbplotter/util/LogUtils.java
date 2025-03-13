package com.dlraudio.dbplotter.util;

import com.dlraudio.dbplotter.controller.LogViewerController;

public class LogUtils {

    /**
     * Log générique (vert)
     */
    public static void log(String message) {
        System.out.println(ColorUtils.colorize("[LOG] " + message, ColorUtils.GREEN));
        appendToUI(message);
    }

    /**
     * Log pour les messages TX (bleu)
     */
    public static void logTx(String message) {
        System.out.println(ColorUtils.colorize("[TX] " + message, ColorUtils.BLUE));
        appendToUI("[TX] " + message);
    }

    /**
     * Log pour les messages RX (cyan)
     */
    public static void logRx(String message) {
        String formattedMessage = "[RX] " + message;

        if (message.contains("ACK")) {
            formattedMessage = "[RX] *" + message + "*";
        }

        System.out.println(ColorUtils.colorize(formattedMessage, ColorUtils.CYAN));
        appendToUI(formattedMessage);
    }

    /**
     * Log pour les erreurs (rouge)
     */
    public static void logError(String message) {
        System.err.println(ColorUtils.colorize("[ERROR] " + message, ColorUtils.RED));
        appendToUI("[ERROR] " + message);
    }

    /**
     * Log pour les avertissements (jaune)
     */
    public static void logWarning(String message) {
        System.out.println(ColorUtils.colorize("[WARNING] " + message, ColorUtils.YELLOW));
        appendToUI("[WARNING] " + message);
    }

    /**
     * Ajoute le message à la fenêtre UI si elle est ouverte
     */
    private static void appendToUI(String message) {
        if (LogViewerController.getInstance() != null) {
            LogViewerController.getInstance().appendLog(message);
        }
    }
}
