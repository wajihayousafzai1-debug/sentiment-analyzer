package com.wajiha.sentiment.controller;

import com.wajiha.sentiment.model.SentimentRequest;
import com.wajiha.sentiment.model.SentimentResult;
import com.wajiha.sentiment.service.SentimentService;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sentiment")
@CrossOrigin("*")
public class SentimentController {

    private final SentimentService service;

    public SentimentController(SentimentService service) {
        this.service = service;
    }

    @PostConstruct
    public void init() {
        System.out.println("🔥 SentimentController LOADED");
    }

    @PostMapping("/analyze")
    public SentimentResult analyze(@RequestBody SentimentRequest request) {
        return service.analyze(request.getText());
    }

    @PostMapping("/batch")
    public List<SentimentResult> batch(@RequestBody List<SentimentRequest> requests) {
        return requests.stream().map(r -> service.analyze(r.getText())).toList();
    }
}
