package util;

import app.config.ThymeleafConfig;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;
import java.util.Properties;

public class GmailEmailSenderHTML {

    private final String username;
    private final String password;
    private final TemplateEngine templateEngine;

    public GmailEmailSenderHTML() {
        // Hent login fra miljøvariabler
        this.username = System.getenv("MAIL_USERNAME");
        this.password = System.getenv("MAIL_PASSWORD");

        if (username == null || password == null) {
            throw new IllegalStateException("MAIL_USERNAME og MAIL_PASSWORD miljøvariabler skal være sat.");
        }

        // Genbrug konfiguration fra ThymeleafConfig
        this.templateEngine = ThymeleafConfig.templateEngine();
    }

    public String renderTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

    public void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setContent(htmlBody, "text/html; charset=UTF-8");

        Transport.send(message);
        System.out.println("HTML-mail sendt til " + to);
    }

    // 🧪 Main-metode til test
    public static void main(String[] args) {
        GmailEmailSenderHTML sender = new GmailEmailSenderHTML();

        String to = "alexmilar1@gmail.com";   // Erstat med din modtager
        String subject = "HTML test med Thymeleaf";

        // Opret en Thymeleaf kontekst med variabler. Tilføj dine egne værdier.
        Map<String, Object> variables = Map.of(
                "title", "Velkommen!",
                "name", "Jon",
                "message", "Dette er en HTML-mail genereret med Thymeleaf og sendt med Gmail SMTP."
        );

        String html = sender.renderTemplate("email.html", variables); // bruger templates/email.html

        try {
            sender.sendHtmlEmail(to, "HTML mail test", html);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
