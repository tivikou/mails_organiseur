package com.example.multiagents.domain.email;

import java.util.List;

/**
 * Port d'accès à une boîte mail. Le domaine ne connaît ni IMAP ni Jakarta Mail :
 * pour supporter un autre protocole (Microsoft Graph, mbox...), on ajoute une implémentation.
 */
public interface MailboxClient {
    /**
     * Récupère les derniers e-mails reçus, du plus récent au plus ancien. Lecture seule :
     * aucun message n'est marqué comme lu, déplacé ou supprimé.
     *
     * @throws MailboxException si la connexion ou l'authentification échoue
     */
    List<Email> fetchRecent(int max);

    /**
     * Envoie une réponse à l'expéditeur de l'email reçu en utilisant la même boîte connectée.
     *
     * @throws MailboxException si l'envoi SMTP échoue
     */
    void sendReply(Email email, String subject, String body);
}
