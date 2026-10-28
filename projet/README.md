# Local Email Agents

Framework Java orienté objet qui propose des brouillons de réponse à des e-mails grâce à des **agents spécialisés** et à un **LLM exécuté localement** (aucune donnée envoyée à un service distant). L'utilisateur garde toujours la validation finale : rien n'est envoyé automatiquement.

## Fonctionnement

1. Onglet **Synchronisation** : connexion à une boîte mail (IMAP, lecture seule) et liste des derniers e-mails ; « Traiter » envoie le mail sélectionné à l'onglet Courrier.
2. Onglet **Modèle LLM** : choix d'Ollama, de l'URL et du modèle, test de connexion, test de rédaction, application à chaud.
3. Onglet **Agents** : l'utilisateur crée un agent pour une tâche précise (nom, description, mots-clés, system prompt) et lui associe un **contexte** : instructions, fichiers de référence et **exemples d'e-mails avec la réponse attendue**.
4. Onglet **Courrier** : à la saisie d'un e-mail, le routeur choisit l'agent, l'agent construit son prompt (contexte + historique réduit) et interroge le LLM local.
5. L'utilisateur voit l'agent choisi, sa confiance, puis **valide, modifie ou rejette** le brouillon. La réponse validée est mémorisée dans le fil de discussion.

```
E-mail → Router → (RoutingStrategy) → SpecializedAgent → PromptBuilder → LocalLanguageModel → brouillon → utilisateur
```

## Organisation du code

| Package | Rôle |
|---|---|
| `domain.agent` | `Agent`, `AgentDefinition` (données persistables), `SpecializedAgent`, `AgentFactory`, `AgentRegistry`, `PromptBuilder`, `AgentRepository` (port) |
| `domain.context` | `AgentContext` : instructions, fichiers, exemples mail/réponse |
| `domain.email` | `Email`, `Conversation`, `ConversationMessage`, `ConversationStore`, `MailboxClient` (port), `MailboxSettings` |
| `routing` | `Router`, `RoutingStrategy`, `KeywordRoutingStrategy`, `FallbackRoutingStrategy` |
| `infrastructure.llm` | `LocalLanguageModel` (interface), `OllamaLanguageModel`, `DemoLocalLanguageModel`, `ConfigurableLanguageModel` (commutable à chaud), `LanguageModelFactory` |
| `infrastructure.mail` | `ImapMailboxClient` (Jakarta Mail) |
| `infrastructure.persistence` | `JsonAgentRepository` |
| `ui` | `MainWindow`, `MailPanel`, `SyncPanel`, `AgentsPanel`, `AgentEditorDialog`, `SettingsPanel` (Swing) |
| `application` | `Application` (point d'entrée, assemblage des dépendances), `AppConfig`, `SettingsService` |

Voir `docs/architecture.md` pour les diagrammes et les patterns, et `docs/PROMPT.md` pour le prompt de conception.

## Prérequis

- JDK 17 ou supérieur, Maven 3.9 ou supérieur.
- Dépendances (téléchargées par Maven) : Jackson 2.18 (JSON), Angus Mail 2.0 (IMAP), JUnit 5 (tests).
- Pour un vrai modèle : [Ollama](https://ollama.com) installé en local, par exemple `ollama pull llama3.2`.

## Compilation et lancement

```bash
mvn clean package
java -jar target/local-email-agents-0.1.0-SNAPSHOT.jar
```

Au premier lancement, trois agents d'exemple sont créés. Le moteur par défaut est **Ollama** : si le serveur n'est pas lancé, un message renvoie vers l'onglet « Modèle LLM ». Le moteur « demo » (réponse factice, hors-ligne) permet de tester l'interface sans modèle.

## Configurer le LLM local

Le plus simple : onglet **Modèle LLM** (lister les modèles installés, « Tester une réponse », « Enregistrer et appliquer »). Les réglages sont écrits dans `config/application.properties`. Sinon, copier `config/application.properties.example` vers `config/application.properties` puis l'adapter :

```properties
llm.provider=ollama
llm.ollama.url=http://localhost:11434
llm.ollama.model=llama3.2
```

Tous les agents reçoivent des règles de rédaction communes (écrire uniquement le texte de l'e-mail, dans la langue de l'expéditeur, sans inventer de faits) avant leur propre system prompt.

## Synchroniser une boîte mail

Onglet **Synchronisation** : serveur IMAP, port (993 en SSL), identifiant, mot de passe, dossier. Gmail, Outlook et iCloud exigent un **mot de passe d'application**. L'accès est en **lecture seule** (aucun mail marqué lu, déplacé ou supprimé) et le **mot de passe n'est jamais enregistré** : il se saisit à chaque session. L'envoi des réponses n'est pas implémenté : vous copiez le brouillon validé dans votre messagerie.

## Agents et autres moteurs

Les agents sont enregistrés dans `data/agents.json` (modifiable à la main). Pour utiliser un autre moteur (LM Studio, llama.cpp...), implémenter `LocalLanguageModel` et l'ajouter à `LanguageModelFactory`.

## Ajouter un agent

Aucun code nécessaire : onglet **Agents → Nouvel agent**. Pour créer un agent par programmation, construire un `AgentDefinition` et appeler `AgentRegistry.save(...)`.

## Limites actuelles et suite

- Routage par mots-clés uniquement (un routeur basé sur le LLM s'ajoute via `RoutingStrategy`).
- Historique des conversations en mémoire (non persisté) ; lecture IMAP seulement, pas d'envoi SMTP.
- À faire : jeu de scénarios d'évaluation comparant plusieurs configurations (modèle, prompt, contexte).
