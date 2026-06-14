//package com.assurance.Service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class EmailServiceImpl implements EmailService {
//    private final JavaMailSender mailSender;
//
//
//    @Override
//    public void sendEmail(String to, String subject, String body) {
//
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(to);
//        message.setSubject(subject);
//        message.setText(body);
//
//        mailSender.send(message);
//    }














    package com.assurance.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

    @Service
    @RequiredArgsConstructor
    public class EmailServiceImpl implements EmailService {
        private final JavaMailSender mailSender;

        @Override
        public void sendEmail(String to, String subject, String body) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        }

        // 🆕 Méthode pour envoyer email HTML stylé
        public void sendHtmlEmail(String to, String subject, String htmlBody) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(htmlBody, true);  // true = HTML
                mailSender.send(message);
                System.out.println("📧 Email envoyé à : " + to);
            } catch (MessagingException e) {
                System.err.println("❌ Erreur envoi email : " + e.getMessage());
            }
        }
    }















