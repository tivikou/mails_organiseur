# Local Email Agents

Framework Java orienté objet pour proposer des réponses à des e-mails à l'aide d'agents spécialisés et d'un LLM exécuté localement.

## Architecture initiale

- `domain` : objets métier indépendants des frameworks.
- `routing` : sélection de l'agent et orchestration du traitement.
- `infrastructure.llm` : abstraction du modèle local et implémentation de démonstration.
- `ui` : interface Swing initiale.
- `application` : composition des dépendances et point d'entrée.

Le modèle `LocalLanguageModel` permet de remplacer `DemoLocalLanguageModel` par un adaptateur Ollama, LM Studio ou tout autre serveur local sans modifier les agents.

## Prérequis

- JDK 17 ou supérieur.
- Maven 3.9 ou supérieur.
- Aucun service distant n'est utilisé par la démonstration.

## Compilation et lancement

```bash
mvn clean package
java -jar target/local-email-agents-0.1.0-SNAPSHOT.jar
```

La démonstration démarre avec trois agents : étudiant, administration et générique. L'interface permet de consulter l'e-mail, l'agent sélectionné et la réponse proposée. L'envoi réel reste volontairement absent : l'utilisateur conserve la validation finale.

## Prochaines étapes

1. Ajouter la création et la modification d'agents dans l'interface.
2. Persister les agents et leurs contextes en JSON ou dans une base locale.
3. Ajouter l'adaptateur HTTP pour le moteur LLM local choisi.
4. Implémenter la gestion complète des threads et la réduction de contexte.
5. Ajouter les tests unitaires et les scénarios d'évaluation du routage.
