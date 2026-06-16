package com.wajiha.sentiment.service.pipeline;

import java.util.Map;

public class SentimentScoringEngine {
    public double computeScore(Map<String, Integer> features) {
        return features.values().stream().mapToDouble(v -> v).sum();
    }
}
