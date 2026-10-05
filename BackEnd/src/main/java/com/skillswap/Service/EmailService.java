package com.skillSwap.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    //Spring’s built-in mail engine (SMTP wrapper)
    private final JavaMailSender mailSender;

    //email sender address (from application.properties)
    private final String from;

    public EmailService(JavaMailSender mailSender, @Value("${skillswap.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void send(String to, String subject, String body) {
        try {

            //Creates a basic text email
            //No attachments, no HTML (plain text only)
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (MailException e) {
            //Used to print errors instead of crashing app
            log.error("Could not send email to {}: {}", to, e.getMessage()); // never break the business flow
        }
    }
}