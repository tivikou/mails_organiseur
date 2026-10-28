package com.example.multiagents.infrastructure.mail;

import com.example.multiagents.domain.email.Email;
import com.example.multiagents.domain.email.MailboxClient;
import com.example.multiagents.domain.email.MailboxException;
import com.example.multiagents.domain.email.MailboxSettings;
import jakarta.mail.Address;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/** Adapter IMAP (Jakarta Mail) : lit les derniers messages d'un dossier, en lecture seule. */
public class ImapMailboxClient implements MailboxClient {
    private final MailboxSettings settings;
    private final char[] password;

    public ImapMailboxClient(MailboxSettings settings, char[] password) {
        this.settings = settings;
        this.password = password;
    }

    @Override
    public List<Email> fetchRecent(int max) {
        Properties props = new Properties();
        String protocol = settings.ssl() ? "imaps" : "imap";
        props.put("mail." + protocol + ".connectiontimeout", "10000");
        props.put("mail." + protocol + ".timeout", "20000");

        Store store = null;
        Folder folder = null;
        try {
            store = Session.getInstance(props).getStore(protocol);
            store.connect(settings.host(), settings.port(), settings.username(), new String(password));
            folder = store.getFolder(settings.folder());
            folder.open(Folder.READ_ONLY); // ne modifie jamais les drapeaux « lu / non lu »
            int count = folder.getMessageCount();
            List<Email> emails = new ArrayList<>();
            // on part du plus récent (numéro le plus élevé) et on s'arrête à `max`
            for (int i = count; i >= 1 && emails.size() < max; i--) {
                emails.add(toEmail(folder.getMessage(i)));
            }
            return emails;
        } catch (MessagingException e) {
            throw new MailboxException("Connexion IMAP impossible : " + e.getMessage(), e);
        } finally {
            closeQuietly(folder, store);
        }
    }

    /** Fermeture explicite : une erreur de fermeture ne doit pas masquer le résultat. */
    private static void closeQuietly(Folder folder, Store store) {
        try {
            if (folder != null && folder.isOpen()) {
                folder.close(false);
            }
        } catch (MessagingException ignored) {
            // rien à faire
        }
        try {
            if (store != null) {
                store.close();
            }
        } catch (MessagingException ignored) {
            // rien à faire
        }
    }

    /** Convertit un message IMAP en {@link Email} (champs vides remplacés par une valeur lisible). */
    private Email toEmail(Message message) throws MessagingException {
        Address[] from = message.getFrom();
        String sender = "(expéditeur inconnu)";
        if (from != null && from.length > 0) {
            sender = from[0] instanceof InternetAddress address ? address.getAddress() : from[0].toString();
        }
        String subject = message.getSubject();
        Date date = message.getReceivedDate() != null ? message.getReceivedDate() : message.getSentDate();
        String body = extractText(message);
        return new Email(
                sender == null || sender.isBlank() ? "(expéditeur inconnu)" : sender,
                subject == null || subject.isBlank() ? "(sans sujet)" : subject,
                body.isBlank() ? "(message sans texte)" : body, // Email refuse un corps vide
                date != null ? date.toInstant() : Instant.now());
    }

    /** Extrait le texte brut ; à défaut, le HTML dépouillé de ses balises. */
    private String extractText(Part part) {
        try {
            if (part.isMimeType("text/plain")) {
                return String.valueOf(part.getContent()).strip();
            }
            if (part.isMimeType("multipart/*")) {
                jakarta.mail.Multipart multipart = (jakarta.mail.Multipart) part.getContent();
                String html = "";
                for (int i = 0; i < multipart.getCount(); i++) {
                    Part child = multipart.getBodyPart(i);
                    if (Part.ATTACHMENT.equalsIgnoreCase(child.getDisposition())) {
                        continue; // les pièces jointes ne sont pas lues
                    }
                    String text = extractText(child);
                    if (child.isMimeType("text/html")) {
                        html = text;
                    } else if (!text.isBlank()) {
                        return text; // text/plain trouvé en priorité
                    }
                }
                return html;
            }
            if (part.isMimeType("text/html")) {
                return String.valueOf(part.getContent()).replaceAll("(?s)<(script|style).*?</\\1>", " ")
                        .replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").strip();
            }
        } catch (MessagingException | IOException e) {
            return "(contenu illisible)";
        }
        return "";
    }
}
