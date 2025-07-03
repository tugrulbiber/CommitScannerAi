package com.example.commitscanner.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Basit mail gönderme metodu
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("turulbiber@gmail.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);

            System.out.println(" Email has been sent to: " + to);
        } catch (Exception e) {
            System.out.println(" Failed to send email. Reason: " + e.getMessage());
        }
    }

    public void sendCommitNotification(String author, String email, String commitHash, String message,
                                       String aiFeedback, String repoName, String fileName, int lineNumber) {
        String subject = " Suspicious Commit Detected";

        String body = "Hello " + author + ",\n\n" +
                "A potentially problematic commit was detected:\n\n" +
                " Repository: " + repoName + "\n" +
                " File: " + fileName + "\n" +
                (lineNumber > 0 ? "📌 Line: " + lineNumber + "\n" : "") +
                " Commit ID: " + commitHash + "\n" +
                " Message: " + message + "\n\n" +
                " AI Feedback:\n" + aiFeedback + "\n\n" +
                "Please double-check this commit.\n\n" +
                "Regards,\nCommitScanner Bot";

        sendSimpleEmail(email, subject, body);
    }

}


