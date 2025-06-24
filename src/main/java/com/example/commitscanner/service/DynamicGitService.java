package com.example.commitscanner.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class DynamicGitService {

    public String getLatestCommitHash(String repoPath) {
        try (Git git = Git.open(new File(repoPath))) {
            Iterable<RevCommit> logs = git.log().setMaxCount(1).call();
            for (RevCommit commit : logs) {
                return commit.getName();
            }
        } catch (Exception e) {
            System.err.println("Failed to get latest commit hash: " + e.getMessage());
        }
        return null;
    }

    public String getLatestCommitMessage(String repoPath) {
        try (Git git = Git.open(new File(repoPath))) {
            Iterable<RevCommit> logs = git.log().setMaxCount(1).call();
            for (RevCommit commit : logs) {
                return commit.getShortMessage();
            }
        } catch (Exception e) {
            System.err.println("Failed to get commit message: " + e.getMessage());
        }
        return null;
    }

    public String getLatestCommitAuthorEmail(String repoPath) {
        try (Git git = Git.open(new File(repoPath))) {
            Iterable<RevCommit> logs = git.log().setMaxCount(1).call();
            for (RevCommit commit : logs) {
                return commit.getAuthorIdent().getEmailAddress();
            }
        } catch (Exception e) {
            System.err.println("Failed to get author email: " + e.getMessage());
        }
        return null;
    }

    public String getLatestCommitAuthorName(String repoPath) {
        try (Git git = Git.open(new File(repoPath))) {
            Iterable<RevCommit> logs = git.log().setMaxCount(1).call();
            for (RevCommit commit : logs) {
                return commit.getAuthorIdent().getName();
            }
        } catch (Exception e) {
            System.err.println("Failed to get author name: " + e.getMessage());
        }
        return null;
    }

    public LocalDateTime getLatestCommitDate(String repoPath) {
        try (Git git = Git.open(new File(repoPath))) {
            Iterable<RevCommit> logs = git.log().setMaxCount(1).call();
            for (RevCommit commit : logs) {
                return Instant.ofEpochSecond(commit.getCommitTime())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
            }
        } catch (Exception e) {
            System.err.println("Failed to get commit date: " + e.getMessage());
        }
        return null;
    }

    public String getDiff(String repoPath, String commitHash) {
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(new File(repoPath + "/.git"))
                    .readEnvironment()
                    .findGitDir()
                    .build();

            try (Git git = new Git(repository)) {
                ObjectId current = repository.resolve(commitHash);
                RevCommit commit = git.log().add(current).setMaxCount(1).call().iterator().next();

                if (commit.getParentCount() == 0) {
                    return "İlk commit, diff yok.";
                }

                ObjectId parent = commit.getParent(0).getTree().getId();
                ObjectId head = commit.getTree().getId();

                CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                try (var reader = repository.newObjectReader()) {
                    oldTreeIter.reset(reader, parent);
                }

                CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                try (var reader = repository.newObjectReader()) {
                    newTreeIter.reset(reader, head);
                }

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                DiffFormatter formatter = new DiffFormatter(out);
                formatter.setRepository(repository);
                List<DiffEntry> entries = formatter.scan(oldTreeIter, newTreeIter);
                for (DiffEntry entry : entries) {
                    formatter.format(entry);
                }

                return out.toString();
            }

        } catch (Exception e) {
            System.err.println("Failed to get diff: " + e.getMessage());
            return "Diff alınamadı.";
        }
    }
}
