# Prompt de conception : framework multi-agents Java pour répondre aux e-mails

> À donner tel quel à un assistant de code (avec le dépôt `mails_organiseur` comme base).

## 1. Rôle

Tu es un architecte logiciel Java senior spécialisé en systèmes multi-agents (SMA) et en intégration de LLM locaux. Tu travailles sur le dépôt existant `projet/` (Maven, Java 17+, Swing) et tu le fais évoluer sans le réécrire : tu réutilises les packages `domain`, `routing`, `infrastructure.llm`, `ui`, `application`.

## 2. Objectif

Concevoir et implémenter un framework Java orienté objet qui aide un utilisateur (ex. enseignant-chercheur) à **proposer des réponses à ses e-mails** grâce à des agents spécialisés. Le texte est généré **exclusivement par un LLM exécuté localement** (aucune donnée envoyée à un service distant). Hypothèse étudiée : une organisation d'agents spécialisés permet d'exploiter efficacement des LLM locaux de taille raisonnable.

## 3. Fonctionnement attendu (cœur du produit)

1. L'utilisateur ouvre une **application graphique** (Swing).
2. Il **crée des agents** pour des tâches précises (étudiants, collègues, administration, projet de recherche, générique, etc.).
3. À la création d'un agent, l'interface demande :
   - un nom et une description courte ;
   - des mots-clés de routage ;
   - un **system prompt** (rôle, ton, structure, informations à ne jamais divulguer, préférences) ;
   - un **contexte** : instructions libres, fichiers de référence, et surtout **des exemples d'e-mails reçus avec la réponse attendue** (few-shot).
4. À la réception d'un e-mail : `E-mail → agent routeur → agent spécialisé → LLM local → agent routeur → utilisateur`.
5. L'utilisateur voit l'e-mail, l'agent choisi (avec confiance et explication), le brouillon proposé, puis **accepte, modifie ou rejette**. Rien n'est envoyé automatiquement.

## 4. Exigences fonctionnelles

- **Routage** : choisir l'agent selon expéditeur, sujet, contenu, fil de discussion et métadonnées (pas uniquement l'adresse). Stratégie remplaçable (`RoutingStrategy`). Gérer le cas « plusieurs agents plausibles » (meilleur score, explication) et « aucun agent adapté » (repli sur l'agent générique sous un seuil de confiance).
- **Spécialisation d'un agent** : system prompt propre + contexte propre, modifiables par l'utilisateur **sans toucher au code** (persistance JSON locale).
- **Gestion des conversations** : mémoire par fil (messages reçus, brouillons générés, réponses réellement envoyées). Ne transmettre au LLM que la partie utile (fenêtre limitée de messages, fichiers de référence tronqués).
- **LLM local interchangeable** : interface `LocalLanguageModel`, implémentation de démonstration hors-ligne et adaptateur HTTP vers un serveur local (Ollama par défaut). Modèle, URL et fournisseur configurables par fichier de configuration.
- **Interface graphique** : lister/créer/modifier/supprimer des agents, éditer le system prompt, associer des fichiers, saisir des exemples mail/réponse, voir l'e-mail, l'agent sélectionné et le brouillon, accepter/modifier/rejeter.

## 5. Exigences d'architecture

- Principes : encapsulation, abstraction, faible couplage, forte cohésion, séparation des responsabilités, extensibilité, réutilisabilité.
- Design patterns **uniquement s'ils répondent à un besoin réel**, avec justification :
  - *Strategy* : routage, construction de mémoire/contexte ;
  - *Factory* : création d'agents depuis leur définition, création du LLM depuis la configuration ;
  - *Repository* : persistance des agents ;
  - *Adapter* : connexion au serveur LLM local ;
  - *Observer* : l'interface se met à jour quand le registre d'agents change ;
  - *Decorator* : seuil de confiance et repli autour d'une stratégie de routage.
- Ajouter un agent, changer un prompt, une source de contexte, le LLM ou la stratégie de routage ne doit **jamais** demander de modifier le noyau.
- Packages : `domain` (modèle métier sans dépendance technique), `routing`, `infrastructure` (llm, persistence), `ui`, `application` (composition des dépendances).

## 6. Exigences de qualité du code

- Code clair, découpé en petites classes à responsabilité unique, **commentaires Javadoc et commentaires de lignes clés en français**, courts et utiles.
- Validation des entrées dans les constructeurs (records immuables quand c'est pertinent).
- Pas de dépendance à un modèle précis dans le domaine. Bibliothèques stables et récentes uniquement.
- Tests unitaires sur le routage, la construction du prompt et la persistance.

## 7. Livrables

1. Code Java complet, compilable avec `mvn clean package`.
2. `README.md` : organisation du code, dépendances, compilation, lancement, configuration du LLM local.
3. `docs/architecture.md` : diagramme de classes et de séquence (Mermaid), organisation des agents, patterns utilisés et justification.
4. Réponses écrites aux 14 questions de conception de l'énoncé (agent, responsabilités, communication, routage, ambiguïté, absence d'agent, contextes, historique, réduction de contexte, ajout d'agent, changement de LLM, persistance, avantages/limites).
5. Un petit jeu de scénarios de test : question d'étudiant, demande d'un collègue, demande administrative, échange sur un projet de recherche, message ambigu.

## 8. Contraintes de réponse

- Avance par étapes : arborescence des packages, interfaces, implémentations, interface graphique, tests, documentation.
- Explique brièvement chaque choix de conception important ; ne liste pas de patterns artificiellement.
- Signale tout piège crucial (sécurité, fuite de données, taille de contexte) mais évite les digressions.
