package com.wajiha.sentiment.service.pipeline;

import java.util.Arrays;
import java.util.List;

public class Tokenizer {
    public List<String> tokenize(String text) {
        return Arrays.asList(text.toLowerCase().split("\\W+"));
    }
}
