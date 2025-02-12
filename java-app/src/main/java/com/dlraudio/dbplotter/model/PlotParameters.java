package com.dlraudio.dbplotter.model;

public class PlotParameters {
    private final double minFrequency;
    private final double maxFrequency;
    private final double minDb;
    private final double maxDb;

    public PlotParameters(double minFrequency, double maxFrequency, double minDb, double maxDb) {
        this.minFrequency = minFrequency;
        this.maxFrequency = maxFrequency;
        this.minDb = minDb;
        this.maxDb = maxDb;
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
