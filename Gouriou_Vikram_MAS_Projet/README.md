# Local Email Agents

Application Java qui génère des brouillons de reponse a des e-mails via des agents specialises et un modele local. Rien n'est envoye automatiquement: l'utilisateur garde toujours la validation finale.

## Prerequis

- JDK 17+
- Maven 3.9+
- Ollama installe en local (recommande pour un usage reel): https://ollama.com

## Dependances du projet

Dependances declarees dans [projet/pom.xml](projet/pom.xml):

- com.fasterxml.jackson.core:jackson-databind:2.18.2 (JSON)
- org.eclipse.angus:angus-mail:2.0.3 (IMAP)
- org.junit.jupiter:junit-jupiter:5.11.4 (tests)

Commandes utiles:

```bash
# Telecharge toutes les dependances (utile hors-ligne ensuite)
mvn dependency:go-offline

# Compile, execute les tests et construit le jar executable
mvn clean package
```

Lancement de l'application:

```bash
java -jar target/local-email-agents-0.1.0-SNAPSHOT.jar
```

## Fonctionnalites et utilisation

### 1) Synchronisation IMAP (lecture seule)

- Connecte une boite mail en IMAP et liste les derniers messages.
- Bouton Traiter: envoie le mail selectionne vers l'onglet Courrier.
- Aucune action destructive: aucun mail marque lu, deplace ou supprime.

### 2) Configuration du modele local

- Onglet Modele LLM: choix du provider, URL, modele, test de connexion, test de reponse.
- Reglages enregistres dans [projet/config/application.properties](projet/config/application.properties).
- Exemple de configuration dans [projet/config/application.properties.example](projet/config/application.properties.example).

Exemple Ollama:

```properties
llm.provider=ollama
llm.ollama.url=http://localhost:11434
llm.ollama.model=llama3.2
```

### 3) Gestion des agents

- Creation/modification d'agents depuis l'onglet Agents.
- Chaque agent peut avoir une description, des mots-cles, un system prompt et un contexte metier.
- Les definitions sont persistees dans [projet/data/agents.json](projet/data/agents.json).

### 4) Generation de brouillons

- Le routeur choisit l'agent le plus adapte.
- L'agent construit le prompt et interroge le modele local.
- L'utilisateur valide, corrige ou rejette le brouillon.

Flux simplifie:

```text
E-mail -> Router -> SpecializedAgent -> PromptBuilder -> LocalLanguageModel -> Brouillon
```

## Connexion d'un compte e-mail

### Parametres IMAP a renseigner

- Serveur IMAP
- Port (generalement 993 en SSL/TLS)
- Identifiant (adresse e-mail complete dans la plupart des cas)
- Mot de passe (ou mot de passe d'application)
- Dossier a lire (ex: INBOX)

Important: le mot de passe n'est pas stocke durablement et doit etre saisi a chaque session.

### Cas Gmail (obligatoire: 2FA + mot de passe d'application)

Pour Gmail, le mot de passe principal du compte ne fonctionne generalement pas en IMAP dans ce type d'application locale.

Procedure:

1. Activer la validation en 2 etapes sur le compte Google.
2. Aller dans Securite -> Mots de passe des applications.
3. Generer un mot de passe d'application (16 caracteres).
4. Utiliser ce mot de passe dans l'onglet Synchronisation a la place du mot de passe habituel.

Parametres Gmail habituels:

- IMAP: imap.gmail.com
- Port: 993
- Securite: SSL/TLS

### Autres fournisseurs

- Outlook et iCloud peuvent aussi exiger un mot de passe d'application.
- En cas d'echec de connexion, verifier en priorite:
	- IMAP active cote fournisseur
	- Port et securite SSL/TLS
	- Presence d'une double authentification et d'un mot de passe d'application
