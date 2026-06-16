package com.wajiha.sentiment.service;

public class SentimentResult {
    public final String sentiment;
    public final double confidence;
    public final String emoji;
    public final String intensity;
    public final int positiveSignals;
    public final int negativeSignals;
    public final String reasoning;

    public SentimentResult(String sentiment, double confidence, String emoji,
                           String intensity, int positiveSignals, int negativeSignals,
                           String reasoning) {
        this.sentiment       = sentiment;
        this.confidence      = confidence;
        this.emoji           = emoji;
        this.intensity       = intensity;
        this.positiveSignals = positiveSignals;
        this.negativeSignals = negativeSignals;
        this.reasoning       = reasoning;
    }

    // Backwards-compatible constructor for any existing callers
    public SentimentResult(String sentiment, double confidence) {
        this(sentiment, confidence, "😐", "Moderate", 0, 0, "");
    }
}
