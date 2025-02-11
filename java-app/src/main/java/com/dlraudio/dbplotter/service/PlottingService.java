package com.dlraudio.dbplotter.service;

import com.dlraudio.dbplotter.model.FrequencyData;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import java.util.List;

public class PlottingService {

    private final LineChart<Number, Number> lineChart;

    public PlottingService(LineChart<Number, Number> lineChart) {
        this.lineChart = lineChart;
    }

    // Initialise le graphique avec des paramètres de base
    public void initializePlot(String xAxisLabel, String yAxisLabel) {
        lineChart.getXAxis().setLabel(xAxisLabel);
        lineChart.getYAxis().setLabel(yAxisLabel);
        lineChart.setAnimated(false);  // Évite les animations lentes
    }

    // Ajoute les données au graphique
    public void plotData(List<FrequencyData> dataPoints) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Frequency Response");

        for (FrequencyData data : dataPoints) {
            series.getData().add(new XYChart.Data<>(data.getFrequency(), data.getMagnitude()));
        }

        lineChart.getData().clear();
        lineChart.getData().add(series);
    }

    // Nettoie les données du graphique
    public void clearPlot() {
        lineChart.getData().clear();
    }
}
