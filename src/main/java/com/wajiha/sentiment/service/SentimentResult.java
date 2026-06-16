package com.wajiha.sentiment.service;

public class SentimentResult {
    public final String sentiment;
    public final double confidence;

    public SentimentResult(String sentiment, double confidence) {
        this.sentiment = sentiment;
        this.confidence = confidence;
    }
}
