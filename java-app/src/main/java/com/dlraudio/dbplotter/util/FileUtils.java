package com.dlraudio.dbplotter.util;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

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
}
