package com.ispas.service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {
    private static final String SENDER_EMAIL = "ispas.notification@gmail.com";
    private static final String SENDER_PASSWORD = "your_app_password_here"; // Set via env var
    
    static {
        // You can set this from environment variable or config file
        String envPassword = System.getenv("ISPAS_EMAIL_PASSWORD");
        if (envPassword != null) {
            // This would require refactoring to use instance variable
        }
    }

    public static void sendRegistrationEmail(String recipientEmail, String customerName, int customerId) {
        try {
            String appPassword = System.getenv("ISPAS_EMAIL_PASSWORD");
            if (appPassword == null) {
                System.out.println("Email service disabled - set ISPAS_EMAIL_PASSWORD environment variable");
                System.out.println("[DEMO] Would send email to: " + recipientEmail);
                return;
            }

            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.connectiontimeout", "5000");
            props.put("mail.smtp.timeout", "5000");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, appPassword);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Welcome to ISP Automation System!");
            message.setText("Hello " + customerName + ",\n\n" +
                    "Thank you for registering with ISP Automation System!\n\n" +
                    "Your Customer ID: " + customerId + "\n" +
                    "Email: " + recipientEmail + "\n\n" +
                    "You can now:\n" +
                    "- Choose from our plans (Basic, Standard, Premium)\n" +
                    "- Track your usage\n" +
                    "- Create support tickets\n" +
                    "- View your bills\n\n" +
                    "Login to your account: http://localhost:4567\n\n" +
                    "Best regards,\n" +
                    "ISP Automation Team");

            Transport.send(message);
            System.out.println("Email sent successfully to: " + recipientEmail);
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            // Don't throw - email failure shouldn't block registration
        }
    }

    public static void sendBillEmail(String recipientEmail, String customerName, double billAmount, int customerId) {
        try {
            String appPassword = System.getenv("ISPAS_EMAIL_PASSWORD");
            if (appPassword == null) {
                System.out.println("[DEMO] Would send bill email to: " + recipientEmail);
                return;
            }

            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, appPassword);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Your ISP Bill - $" + String.format("%.2f", billAmount));
            message.setText("Hello " + customerName + ",\n\n" +
                    "Your current bill statement:\n\n" +
                    "Customer ID: " + customerId + "\n" +
                    "Total Amount Due: $" + String.format("%.2f", billAmount) + "\n" +
                    "Date: " + new java.util.Date() + "\n\n" +
                    "Please log in to make a payment: http://localhost:4567\n\n" +
                    "Thank you for choosing our service!\n\n" +
                    "Best regards,\n" +
                    "ISP Automation Team");

            Transport.send(message);
            System.out.println("Bill email sent successfully to: " + recipientEmail);
        } catch (MessagingException e) {
            System.err.println("Failed to send bill email: " + e.getMessage());
        }
    }
}
