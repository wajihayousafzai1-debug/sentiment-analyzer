package com.wajiha.sentiment.service;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.springframework.stereotype.Service;
import java.util.Properties;

@Service
public class SentimentService {

    private final StanfordCoreNLP pipeline;

    public SentimentService() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        this.pipeline = new StanfordCoreNLP(props);
    }

    public SentimentResult analyze(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new SentimentResult("Neutral", 0.5);
        }

        // Create and annotate document
        CoreDocument document = new CoreDocument(text);
        pipeline.annotate(document);

        int totalSentiment = 0;
        int sentenceCount = 0;

        for (CoreSentence sentence : document.sentences()) {
            Tree sentimentTree = sentence.sentimentTree();  // or use CoreMap way below if needed
            int sentiment = RNNCoreAnnotations.getPredictedClass(sentimentTree);
            
            totalSentiment += sentiment;  // 0=very negative ... 4=very positive
            sentenceCount++;
        }

        double avgSentiment = sentenceCount > 0 ? (double) totalSentiment / sentenceCount : 2.0;

        String sentimentLabel;
        double confidence = 0.75 + (Math.abs(avgSentiment - 2.0) * 0.12);  // slightly tuned

        if (avgSentiment >= 3.0) {
            sentimentLabel = "Positive";
        } else if (avgSentiment <= 1.0) {
            sentimentLabel = "Negative";
        } else {
            sentimentLabel = "Neutral";
        }

        return new SentimentResult(sentimentLabel, Math.min(confidence, 0.98));
    }
}
