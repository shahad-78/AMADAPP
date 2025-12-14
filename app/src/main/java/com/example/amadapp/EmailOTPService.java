package com.example.amadapp;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailOTPService {

    // Configure these with your email provider settings
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_USERNAME = "shahadalhamli24@gmail.com";
    private static final String EMAIL_PASSWORD = "bppu zywv pgmb afsa"; // App password for Gmail
    //https://myaccount.google.com/apppasswords
    public boolean sendOTPEmail(String recipientEmail, String otp) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", SMTP_HOST);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Your OTP Verification Code");

            String emailContent = buildEmailContent(otp);
            message.setContent(emailContent, "text/html");

            Transport.send(message);
            return true;

        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String buildEmailContent(String otp) {
        return "<html><body>" +
                "<h2>OTP Verification Code</h2>" +
                "<p>Your One-Time Password (OTP) is:</p>" +
                "<h1 style='color: #1D5450; font-size: 32px; letter-spacing: 5px;'>" + otp + "</h1>" +
                "<p>If you didn't request this, please ignore this email.</p>" +
                "<br><p>Best regards,<br>Amad App Team</p>" +
                "</body></html>";
    }
}
