package com.wajiha.sentiment.model;

public class SentimentResult {
    private String sentiment;
    private double confidence;
    private double score;

    public SentimentResult(String sentiment, double confidence, double score) {
        this.sentiment = sentiment;
        this.confidence = confidence;
        this.score = score;
    }

    public String getSentiment() { return sentiment; }
    public double getConfidence() { return confidence; }
    public double getScore() { return score; }
}
