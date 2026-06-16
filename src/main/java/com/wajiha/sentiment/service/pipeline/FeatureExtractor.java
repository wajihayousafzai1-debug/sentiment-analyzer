package com.wajiha.sentiment.service.pipeline;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureExtractor {

    private static final Map<String, Integer> LEXICON = new HashMap<>();

    static {
        LEXICON.put("good", 1);
        LEXICON.put("great", 2);
        LEXICON.put("excellent", 3);
        LEXICON.put("bad", -1);
        LEXICON.put("terrible", -3);
        LEXICON.put("love", 2);
        LEXICON.put("hate", -2);
    }

    public Map<String, Integer> extract(List<String> tokens) {
        Map<String, Integer> features = new HashMap<>();

        for (String t : tokens) {
            if (LEXICON.containsKey(t)) {
                features.put(t, LEXICON.get(t));
            }
        }
        return features;
    }
}
