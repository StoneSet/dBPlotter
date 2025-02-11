package com.dlraudio.dbplotter.model;

public class PlotParameters {
    private double minFrequency;
    private double maxFrequency;
    private double minDb;
    private double maxDb;
    private double paperSpeed;

    public PlotParameters(double minFrequency, double maxFrequency, double minDb, double maxDb, double paperSpeed) {
        this.minFrequency = minFrequency;
        this.maxFrequency = maxFrequency;
        this.minDb = minDb;
        this.maxDb = maxDb;
        this.paperSpeed = paperSpeed;
    }

    public double getMinFrequency() {
        return minFrequency;
    }

    public double getMaxFrequency() {
        return maxFrequency;
    }

    public double getMinDb() {
        return minDb;
    }

    public double getMaxDb() {
        return maxDb;
    }

}
