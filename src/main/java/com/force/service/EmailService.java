package com.force.service;

import java.util.Properties;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

@ApplicationScoped
public class EmailService {

    @ConfigProperty(name = "sales.mail.smtp.host", defaultValue = "smtp.office365.com") String smtpHost;
    @ConfigProperty(name = "sales.mail.smtp.port", defaultValue = "587") String smtpPort;
    @ConfigProperty(name = "sales.mail.smtp.auth", defaultValue = "true") String smtpAutBool;
    @ConfigProperty(name = "sales.mail.starttls.enable", defaultValue = "true") String smtpStarttlsEnable;

    @ConfigProperty(name = "sales.mail.sender.user", defaultValue = "serv.serradouradadb@gmail.com") String smtpSenderUser;
    @ConfigProperty(name = "sales.mail.sender.pass", defaultValue = "kljg mxdt jpdk erbu") String smtpSenderPass;
    
    public void sendEmailSMTP(String receiverEmails, String subjectText, String htmlContent) throws MessagingException {

        // Mail server properties
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.auth", smtpAutBool);
        props.put("mail.smtp.starttls.enable", smtpStarttlsEnable);

        Session session = Session.getInstance(props,
                new jakarta.mail.Authenticator() {
                    @Override
                    protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new jakarta.mail.PasswordAuthentication(smtpSenderUser, smtpSenderPass);
                    }
                });

        // Create MimeMessage
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(smtpSenderUser));
        message.setSubject(subjectText);
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiverEmails));

        // Create MimeBodyPart for HTML content
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlContent, "text/html; charset=UTF-8");

        // Create Multipart
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(htmlPart);

        // Set content
        message.setContent(multipart);

        // Send message
        Transport.send(message);
    }

}
