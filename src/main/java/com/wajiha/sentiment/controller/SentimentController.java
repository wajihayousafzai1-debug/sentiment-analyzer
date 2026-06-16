package com.wajiha.sentiment.controller;

import com.wajiha.sentiment.service.SentimentResult;
import com.wajiha.sentiment.service.SentimentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SentimentController {

    private final SentimentService sentimentService;

    public SentimentController(SentimentService sentimentService) {
        this.sentimentService = sentimentService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/analyze")
    public String analyze(@RequestParam String text, Model model) {
        SentimentResult result = sentimentService.analyze(text);
        model.addAttribute("text", text);
        model.addAttribute("sentiment", result.sentiment);
        model.addAttribute("confidence", String.format("%.1f", result.confidence * 100));
        return "index";
    }
}
