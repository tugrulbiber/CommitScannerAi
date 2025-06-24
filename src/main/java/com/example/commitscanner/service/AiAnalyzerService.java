package com.example.commitscanner.service;

import org.springframework.stereotype.Service;

@Service
public class AiAnalyzerService {

    private final AiAnalyzer aiAnalyzer = new AiAnalyzer();

    public String analyzeCommit(String commitMessage, String diff) {
        return aiAnalyzer.analyze(commitMessage, diff);
    }
}
