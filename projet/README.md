# Local Email Agents

Framework Java orienté objet qui propose des brouillons de réponse à des e-mails grâce à des **agents spécialisés** et à un **LLM exécuté localement** (aucune donnée envoyée à un service distant). L'utilisateur garde toujours la validation finale : rien n'est envoyé automatiquement.

## Fonctionnement

1. Onglet **Agents** : l'utilisateur crée un agent pour une tâche précise (nom, description, mots-clés, system prompt) et lui associe un **contexte** : instructions, fichiers de référence et **exemples d'e-mails avec la réponse attendue**.
2. Onglet **Courrier** : à la saisie d'un e-mail, le routeur choisit l'agent, l'agent construit son prompt (contexte + historique réduit) et interroge le LLM local.
3. L'utilisateur voit l'agent choisi, sa confiance, puis **valide, modifie ou rejette** le brouillon. La réponse validée est mémorisée dans le fil de discussion.

```
E-mail → Router → (RoutingStrategy) → SpecializedAgent → PromptBuilder → LocalLanguageModel → brouillon → utilisateur
```

## Organisation du code

| Package | Rôle |
|---|---|
| `domain.agent` | `Agent`, `AgentDefinition` (données persistables), `SpecializedAgent`, `AgentFactory`, `AgentRegistry`, `PromptBuilder`, `AgentRepository` (port) |
| `domain.context` | `AgentContext` : instructions, fichiers, exemples mail/réponse |
| `domain.email` | `Email`, `Conversation`, `ConversationMessage`, `ConversationStore` |
| `routing` | `Router`, `RoutingStrategy`, `KeywordRoutingStrategy`, `FallbackRoutingStrategy` |
| `infrastructure.llm` | `LocalLanguageModel` (interface), `OllamaLanguageModel`, `DemoLocalLanguageModel`, `LanguageModelFactory` |
| `infrastructure.persistence` | `JsonAgentRepository` |
| `ui` | `MainWindow`, `MailPanel`, `AgentsPanel`, `AgentEditorDialog` (Swing) |
| `application` | `Application` (point d'entrée, assemblage des dépendances), `AppConfig` |

Voir `docs/architecture.md` pour les diagrammes et les patterns, et `docs/PROMPT.md` pour le prompt de conception.

## Prérequis

- JDK 17 ou supérieur, Maven 3.9 ou supérieur.
- Dépendances (téléchargées par Maven) : Jackson 2.18 (JSON), JUnit 5 (tests).
- Pour un vrai modèle : [Ollama](https://ollama.com) installé en local, par exemple `ollama pull llama3.2`.

## Compilation et lancement

```bash
mvn clean package
java -jar target/local-email-agents-0.1.0-SNAPSHOT.jar
```

Sans configuration, l'application démarre en **mode démonstration** (réponse factice, hors-ligne) avec trois agents d'exemple.

## Configurer le LLM local

Copier `config/application.properties.example` vers `config/application.properties` puis l'adapter :

```properties
llm.provider=ollama
llm.ollama.url=http://localhost:11434
llm.ollama.model=llama3.2
```

Les agents sont enregistrés dans `data/agents.json` (modifiable à la main). Pour utiliser un autre moteur (LM Studio, llama.cpp...), implémenter `LocalLanguageModel` et l'ajouter à `LanguageModelFactory`.

## Ajouter un agent

Aucun code nécessaire : onglet **Agents → Nouvel agent**. Pour créer un agent par programmation, construire un `AgentDefinition` et appeler `AgentRegistry.save(...)`.

## Limites actuelles et suite

- Routage par mots-clés uniquement (un routeur basé sur le LLM s'ajoute via `RoutingStrategy`).
- Historique des conversations en mémoire (non persisté) ; pas de connexion IMAP/SMTP.
- À faire : jeu de scénarios d'évaluation comparant plusieurs configurations (modèle, prompt, contexte).
