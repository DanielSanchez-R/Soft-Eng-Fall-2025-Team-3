package util;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * EmailUtil provides a simple utility for sending HTML emails using
 * Gmail's SMTP service through the JavaMail API.
 * <p>
 * This utility is primarily used for sending automated notifications,
 * such as password reset links, staff invitations, and order confirmations
 * within the restaurant booking and ordering system.
 * </p>
 *
 * <h3>Features:</h3>
 * <ul>
 *   <li>Uses Gmail SMTP with TLSv1.2 security.</li>
 *   <li>Supports HTML-formatted email content with UTF-8 encoding.</li>
 *   <li>Displays detailed SMTP debug output in the console.</li>
 *   <li>Handles both authentication and connection errors gracefully.</li>
 * </ul>
 *
 * <h3>Configuration:</h3>
 * <ul>
 *   <li>SMTP Host: <b>smtp.gmail.com</b></li>
 *   <li>Port: <b>587</b></li>
 *   <li>Encryption: <b>STARTTLS (TLSv1.2)</b></li>
 *   <li>Authentication: Required</li>
 * </ul>
 *
 * <p><b>Note:</b> The sender account must have a valid Gmail App Password
 * (16-character key) configured for secure authentication. This password
 * should be stored securely and not hardcoded in production environments.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * EmailUtil.sendEmail(
 *     "user@example.com",
 *     "Welcome to Pizzas 505!",
 *     "<h1>Thank you for registering!</h1><p>Your account is now active.</p>"
 * );
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public class EmailUtil {

    /**
     * Sends an HTML email using Gmail's SMTP service.
     * <p>
     * The email is constructed using the provided recipient address, subject line,
     * and body content. The body supports HTML formatting.
     * </p>
     *
     * <p>Includes full SMTP debug output for diagnostic purposes.</p>
     *
     * @param to      the recipient's email address
     * @param subject the subject line of the email
     * @param body    the body content of the email (supports HTML)
     */
    public static void sendEmail(String to, String subject, String body) {
        final String from = "ds86danielsanchez@gmail.com"; // Sender email; can be changed
        final String password = "fmlyistyrjdncvzu";         // Gmail App Password (16-char)

        // --- Gmail SMTP configuration ---
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.debug", "true"); // Print full SMTP debug info

        // Create authenticated session
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            System.out.println(" EmailUtil: preparing to send email to " + to);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from, "Pizzas 505 ENMU"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(body, "text/html; charset=UTF-8");

            Transport.send(message);

            System.out.println(" EmailUtil: message sent successfully to " + to);

        } catch (MessagingException mex) {
            System.out.println("❌ EmailUtil: MessagingException occurred!");
            mex.printStackTrace(System.out);
            Exception nextEx = mex.getNextException();
            if (nextEx != null) nextEx.printStackTrace(System.out);
        } catch (Exception e) {
            System.out.println("❌ EmailUtil: General Exception occurred!");
            e.printStackTrace(System.out);
        }
    }
}
