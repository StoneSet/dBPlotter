package com.dlraudio.dbplotter.util;

public class ColorUtils {

    // Couleurs ANSI pour la console
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";

    /**
     * Applique une couleur à un message pour la console
     */
    public static String colorize(String message, String color) {
        return color + message + RESET;
    }
}
