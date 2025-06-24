package com.example.commitscanner.scheduler;

import com.example.commitscanner.entity.CommitRecord;
import com.example.commitscanner.service.*;
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
            "https://github.com/tugrulbiber/leave_management_ai",
            "https://github.com/tugrulbiber/CommitScannerAi",
            "https://github.com/ozcangunatis/commitscanner",
            "https://github.com/ozcangunatis/management-backend"
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

    @Scheduled(fixedRate = 60000) // test için her dakika
    public void scanAllRepos() {
        AutoRepoManager.prepareRepositories(repoUrls);

        for (String url : repoUrls) {
            String repoName = extractRepoName(url);
            String repoPath = "repos/" + repoName;

            List<String> commitHashes = gitService.getLastCommitHashes(repoPath, 3); // son 3 commit
            if (commitHashes == null || commitHashes.isEmpty()) {
                System.out.println("❌ Commit bulunamadı: " + repoPath);
                continue;
            }

            for (String commitHash : commitHashes) {

                if (commitService.existsByCommitHash(commitHash)) {
                    System.out.println("⏭ Commit zaten işlenmiş, atlanıyor: " + commitHash);
                    continue;
                }

                String author = gitService.getCommitAuthorNameByHash(repoPath, commitHash);
                String message = gitService.getCommitMessageByHash(repoPath, commitHash);
                String diff = gitService.getCommitDiffByHash(repoPath, commitHash);
                String email = "turulbiber@gmail.com"; // ileride dinamik yapılabilir

                String feedback = aiAnalyzerService.analyzeCommit(message, diff);
                boolean hasIssue = feedback.toLowerCase().contains("risk")
                        || feedback.toLowerCase().contains("hata")
                        || feedback.toLowerCase().contains("daha iyi");

                emailService.sendCommitNotification(author, email, commitHash,
                        message + "\n\nAI Feedback:\n" + feedback);

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
    }

    private String extractRepoName(String url) {
        return url.substring(url.lastIndexOf('/') + 1).replace(".git", "");
    }
}
