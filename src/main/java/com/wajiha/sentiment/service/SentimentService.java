package com.wajiha.sentiment.service;

import com.wajiha.sentiment.model.SentimentResult;
import com.wajiha.sentiment.service.pipeline.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SentimentService {

    private final Tokenizer tokenizer = new Tokenizer();
    private final FeatureExtractor extractor = new FeatureExtractor();
    private final SentimentScoringEngine engine = new SentimentScoringEngine();
    private final ConfidenceEstimator estimator = new ConfidenceEstimator();

    public SentimentResult analyze(String text) {

        if (text == null || text.isBlank()) {
            return new SentimentResult("Neutral", 0.5, 0);
        }

        List<String> tokens = tokenizer.tokenize(text);
        Map<String, Integer> features = extractor.extract(tokens);

        double score = engine.computeScore(features);
        double confidence = estimator.computeConfidence(score);

        String sentiment = classify(score);

        return new SentimentResult(sentiment, confidence, score);
    }

    private String classify(double score) {
        if (score > 1) return "Positive";
        if (score < -1) return "Negative";
        return "Neutral";
    }
}
