package com.dlraudio.dbplotter.util;

import com.dlraudio.dbplotter.model.FrequencyData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CsvImporter {

    public List<FrequencyData> importFromRew(File csvFile) {
        return importRewCsv(csvFile);
    }

    public List<FrequencyData> importFromArta(File csvFile) {
        return importCsv(csvFile, ",");
    }

    /**
     * Importe un fichier CSV REW avec une gestion correcte des colonnes et espaces.
     */
    private List<FrequencyData> importRewCsv(File csvFile) {
        List<FrequencyData> dataPoints = new ArrayList<>();
        boolean dataSectionStarted = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.strip();
                //System.out.println("Read line: " + line);

                // Ignorer les lignes de métadonnées ou vides
                if (line.isEmpty()) {
                    continue;
                }

                // Détecter le début des données
                if (!dataSectionStarted) {
                    // Supprimer l'astérisque et vérifier si c'est la ligne de titre
                    String cleanLine = line.startsWith("*") ? line.substring(1).trim() : line;
                    if (cleanLine.matches("(?i)^freq.*spl.*phase.*$")) {
                        //System.out.println("Data section header detected.");
                        dataSectionStarted = true;
                        continue;
                    } else {
                        //DEBUG
                        //System.out.println("Header not detected yet.");
                    }
                    continue;
                }

                // Lire les lignes de données
                String[] values = line.split("\\s+");
                //System.out.println("Split values: " + String.join(", ", values));

                if (values.length >= 2) {
                    try {
                        double frequency = Double.parseDouble(values[0]);
                        double magnitude = Double.parseDouble(values[1]);
                        dataPoints.add(new FrequencyData(frequency, magnitude));
                        //System.out.println("Added data point: Frequency=" + frequency + ", Magnitude=" + magnitude);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid number format in line: " + line);
                    }
                } else {
                    System.err.println("Insufficient data in line: " + line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Total data points imported: " + dataPoints.size());
        return dataPoints;
    }

    /**
     * Importe un fichier CSV d'ARTA avec un séparateur défini.
     */
    private List<FrequencyData> importCsv(File csvFile, String delimiter) {
        List<FrequencyData> dataPoints = new ArrayList<>();
        boolean dataSectionStarted = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.strip();

                // Détecter la ligne de titre des colonnes
                if (!dataSectionStarted && line.equalsIgnoreCase("Frequency(Hz), Magnitude(dB)")) {
                    dataSectionStarted = true;
                    continue;
                }

                if (dataSectionStarted) {
                    String[] values = line.split(delimiter);
                    if (values.length >= 2) {
                        try {
                            double frequency = Double.parseDouble(values[0].trim());
                            double magnitude = Double.parseDouble(values[1].trim());
                            dataPoints.add(new FrequencyData(frequency, magnitude));
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid data line: " + line);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Total data points imported: " + dataPoints.size());
        return dataPoints;
    }
}
