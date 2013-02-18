package de.uni_mannheim.informatik.dws.goldminer.util;

/**
 * Tuple containing support and confidence values both being double values
 */
public class SupportConfidenceTuple {
    private double support;
    private double confidence;

    public SupportConfidenceTuple(double support, double confidence) {
        this.support = support;
        this.confidence = confidence;
    }

    public double getSupport() {
        return support;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setSupport(double support) {
        this.support = support;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}