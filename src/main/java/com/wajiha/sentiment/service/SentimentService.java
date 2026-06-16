package com.wajiha.sentiment.service;

import org.springframework.stereotype.Service;

@Service
public class SentimentService {

    public SentimentResult analyze(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new SentimentResult("Neutral", 0.5);
        }

        String lowerText = text.toLowerCase().trim();
        int positiveCount = countMatches(lowerText, new String[]{"good", "great", "excellent", "amazing", "love", "happy", "wonderful", "best", "fantastic"});
        int negativeCount = countMatches(lowerText, new String[]{"bad", "terrible", "awful", "hate", "sad", "worst", "poor", "horrible", "disappointed"});

        String sentiment;
        double confidence;

        if (positiveCount > negativeCount) {
            sentiment = "Positive";
            confidence = 0.65 + (positiveCount * 0.08);
        } else if (negativeCount > positiveCount) {
            sentiment = "Negative";
            confidence = 0.65 + (negativeCount * 0.08);
        } else {
            sentiment = "Neutral";
            confidence = 0.5;
        }

        return new SentimentResult(sentiment, Math.min(confidence, 0.98));
    }

    private int countMatches(String text, String[] words) {
        int count = 0;
        for (String word : words) {
            if (text.contains(word)) {
                count++;
            }
        }
        return count;
    }
}

