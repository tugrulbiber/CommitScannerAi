package com.example.commitscanner.service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // UTF-8 destekli mail gönderme metodu
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            helper.setFrom(new InternetAddress("turulbiber@gmail.com", "CommitScanner Bot", "UTF-8"));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false); // false = plain text, true = HTML

            mailSender.send(mimeMessage);

            System.out.println("✅ Email has been sent to: " + to);
        } catch (Exception e) {
            System.out.println("❌ Failed to send email. Reason: " + e.getMessage());
        }
    }

    public void sendCommitNotification(String author, String email, String commitHash, String message,
                                       String aiFeedback, String repoName, String fileName, int lineNumber) {
        String subject = "Şüpheli Commit Tespit Edildi";

        String body = "Merhaba " + author + ",\n\n" +
                "Potansiyel olarak sorunlu bir commit tespit edildi:\n\n" +
                " Repository: " + repoName + "\n" +
                " Dosya: " + fileName + "\n" +
                (lineNumber > 0 ? "🔢 Satır: " + lineNumber + "\n" : "") +
                " Commit ID: " + commitHash + "\n" +
                " Commit Mesajı: " + message + "\n\n" +
                " AI Geri Bildirimi:\n" + aiFeedback + "\n\n" +
                "Lütfen bu commit’i tekrar gözden geçirin.\n\n" +
                "Saygılar,\nCommitScanner Bot";

        sendSimpleEmail(email, subject, body);
    }
}
