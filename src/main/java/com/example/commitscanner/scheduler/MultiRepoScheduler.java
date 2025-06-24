package com.example.commitscanner.scheduler;

import com.example.commitscanner.entity.CommitRecord;
import com.example.commitscanner.service.*;
import com.example.commitscanner.service.AutoRepoManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class MultiRepoScheduler {

    private final DynamicGitService gitService;
    private final EmailService emailService;
    private final CommitService commitService;
    private final AiAnalyzerService aiAnalyzerService;

    private final List<String> repoUrls = List.of(
            "https://github.com/tugrulbiber/leave_management_ai"
            // buraya istediğin kadar repo ekleyebilirsin
    );

    public MultiRepoScheduler(DynamicGitService gitService,
                              EmailService emailService,
                              CommitService commitService,
                              AiAnalyzerService aiAnalyzerService) {
        this.gitService = gitService;
        this.emailService = emailService;
        this.commitService = commitService;
        this.aiAnalyzerService = aiAnalyzerService;
    }

    @Scheduled(fixedRate = 60000) // her dakika test için
    public void scanAllRepos() {
        AutoRepoManager.prepareRepositories(repoUrls);

        for (String url : repoUrls) {
            String repoName = extractRepoName(url);
            String repoPath = "repos/" + repoName;

            String commitHash = gitService.getLatestCommitHash(repoPath);
            if (commitHash == null) {
                System.out.println("❌ Commit bulunamadı: " + repoPath);
                continue;
            }

            // Commit bilgilerini al
            String author = gitService.getLatestCommitAuthorName(repoPath);
            String email = gitService.getLatestCommitAuthorEmail(repoPath);
            String message = gitService.getLatestCommitMessage(repoPath);
            String diff = gitService.getDiff(repoPath, commitHash);

            // AI feedback al
            String feedback = aiAnalyzerService.analyzeCommit(message, diff);
            boolean hasIssue = feedback.toLowerCase().contains("risk")
                    || feedback.toLowerCase().contains("hata")
                    || feedback.toLowerCase().contains("daha iyi");

            // Mail gönder
            emailService.sendCommitNotification(author, email, commitHash, message + "\n\nAI Feedback:\n" + feedback);

            // Commit kaydet
            CommitRecord record = new CommitRecord();
            record.setCommitHash(commitHash);
            record.setAuthorName(author);
            record.setAuthorEmail(email);
            record.setMessage(message);
            record.setCommitDate(LocalDateTime.now());
            record.setHasIssue(hasIssue);
            record.setAiFeedback(feedback);
            record.setScannedAt(LocalDateTime.now());

            commitService.saveCommit(record);

            System.out.println("✅ " + repoName + " → Commit işlendi: " + commitHash);
        }
    }

    private String extractRepoName(String url) {
        return url.substring(url.lastIndexOf('/') + 1).replace(".git", "");
    }
}
