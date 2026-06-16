package com.wajiha.sentiment.service.pipeline;

public class ConfidenceEstimator {
    public double computeConfidence(double score) {
        double abs = Math.abs(score);
        double c = 1 - Math.exp(-abs / 3.0);
        return Math.min(0.99, Math.max(0.5, c));
    }
}
