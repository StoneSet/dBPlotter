package com.dlraudio.dbplotter.util;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    /**
     * Opens a FileChooser dialog to select a CSV file.
     *
     * @param ownerWindow The parent window for the dialog.
     * @return The selected file, or null if the user cancels.
     */
    public static File selectCsvFile(Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        return fileChooser.showOpenDialog(ownerWindow);
    }

    /**
     * Reads a CSV file and returns the data as a list of string arrays.
     *
     * @param filePath  The path to the CSV file.
     * @param separator The separator used in the CSV file (e.g., comma, semicolon).
     * @return A list of string arrays, where each array represents a line in the CSV file.
     * @throws IOException If an error occurs while reading the file.
     */
    public static List<String[]> readCsvFile(String filePath, String separator) throws IOException {
        List<String[]> data = new ArrayList<>();
        Path path = Paths.get(filePath);

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] fields = line.split(separator);
                    data.add(fields);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            throw e;
        }

        return data;
    }

    /**
     * Reads a CSV file using a comma (,) as the default separator.
     *
     * @param filePath The path to the CSV file.
     * @return A list of string arrays representing the CSV data.
     * @throws IOException If an error occurs while reading the file.
     */
    public static List<String[]> readCsvFile(String filePath) throws IOException {
        return readCsvFile(filePath, ",");
    }
}
