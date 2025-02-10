package com.dlraudio.ui;

import javax.swing.*;
import java.awt.*;

public class AppUI {
    public void startUI() {
        JFrame frame = new JFrame("dbPlotter - Application de Mesure");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JButton importButton = new JButton("Importer fichier CSV");
        mainPanel.add(importButton, BorderLayout.NORTH);

        JTable dataTable = new JTable();
        mainPanel.add(new JScrollPane(dataTable), BorderLayout.CENTER);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        mainPanel.add(progressBar, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);
    }
}
