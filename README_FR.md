# AIHoneypot - Système de Détection d'Agents IA

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-green)
![Maven](https://img.shields.io/badge/Maven-Multi--Module-blue)
![License](https://img.shields.io/badge/License-MIT-blue)

Un système honeypot sophistiqué pour détecter et classifier les agents IA, les bots et les clients HTTP automatisés en utilisant l'empreinte comportementale et l'apprentissage automatique.

---

## 🌍 Documentation dans d'Autres Langues

- 🇬🇧 [English](README.md)
- 🇮🇹 [Italiano](README_IT.md)
- 🇫🇷 [Français](README_FR.md) (Actuel)
- 🇪🇸 [Español](README_ES.md)
- 🇩🇪 [Deutsch](README_DE.md)
- 🇨🇳 [简体中文](README_ZH.md)
- 🇯🇵 [日本語](README_JA.md)
- 🇷🇺 [Русский](README_RU.md)

---

## 🎯 Fonctionnalités

- **🎨 Tableau de Bord GUI Interactif**: Belle application de bureau JavaFX avec surveillance en temps réel
  - Statistiques de menaces en direct avec actualisation automatique
  - Graphiques interactifs (camembert et barres) pour la visualisation des données
  - Table des menaces récentes avec informations détaillées
  - Analyse des IP attaquantes principales
  - Thème moderne inspiré d'iOS avec animations fluides
  
- **🚗 Simulateur de Trafic**: Génère un trafic honeypot réaliste pour les tests
  - Génération automatique de trafic toutes les 5-30 secondes
  - Modèles d'attaque réalistes (injection SQL, XSS, scans de bots)
  - Vagues de trafic en rafale pour les tests de charge
  - Plusieurs types de clients (humains, bots, agents IA, scanners)

- **🌱 Peuplement de Base de Données**: Pré-remplit la base de données avec des données historiques
  - 255+ sessions de menaces initiales
  - 7 jours de modèles historiques
  - Distributions d'attaques réalistes par gravité

- **🔍 Empreinte Comportementale**: Analyse les modèles de requêtes HTTP pour distinguer les humains des bots

- **🧠 Classification Multi-Niveaux**: 
  - Détection basée sur des règles pour les modèles connus
  - Isolation Forest pour la détection d'anomalies
  - Méthodes d'ensemble combinant plusieurs classificateurs

- **🐦 Détection de Bots X (Twitter)**: Module spécialisé pour détecter les bots et agents IA sur les réseaux sociaux
  - Analyse de profil (âge, modèles de nom d'utilisateur, détection de bio)
  - Analyse réseau (ratios follower/following)
  - Modèles temporels (fréquence de publication)
  - Détection de texte généré par IA

- **🕸️ Pièges Canary**: Points d'accès leurres qu'aucun utilisateur légitime ne devrait accéder
  - `/admin`, `/wp-admin`, `/.env`, `/backup`, `/.git` et plus
  - Gravité CRITIQUE automatique lors de l'accès

- **💾 Journalisation des Menaces en Temps Réel**: Persiste les sessions de menaces dans une base de données H2/PostgreSQL

- **📊 Tableau de Bord API REST**: Surveille et analyse les menaces détectées via des points de terminaison HTTP

- **🔄 Suivi de Session**: Corrèle plusieurs requêtes de la même session

- **🔒 Analyse de Sécurité**: Scanner de sécurité de sites Web avec analyse à 7 niveaux
  - Validation SSL/TLS
  - Vérification des en-têtes de sécurité
  - Scan de ports
  - Détection de vulnérabilités

## 🏗️ Architecture

Ceci est un projet Maven multi-modules avec la structure suivante:

```
AIHoneypot/
├── core/              # Modèles de domaine, interfaces, exceptions
├── collector/         # Couche de collecte de signaux (filtres servlet)
├── analyzer/          # Moteur de classification des menaces
├── dashboard/         # API REST et statistiques
├── x-detector/        # Module de détection de bots X (Twitter)
└── honeypot/          # Application Spring Boot principale + pièges canari
```

### Dépendances des Modules

```
honeypot (principal)
├── dashboard
│   └── analyzer
│       └── core
├── collector
│   └── core
├── x-detector
│   └── core
└── analyzer
    └── core
```

## 🚀 Démarrage Rapide

### Prérequis

- Java 17+
- Maven 3.8+
- (Optionnel) PostgreSQL pour la production

### Build

```bash
mvn clean install
```

### Exécution

```bash
cd honeypot
mvn spring-boot:run
```

L'application démarrera sur `http://localhost:8080`

### Accès au Tableau de Bord

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Console H2**: http://localhost:8080/h2-console
- **Documentation API**: http://localhost:8080/api-docs

## 📊 Classes de Domaine Core

### Module Core (10 classes)

1. **ClientType** (enum) - HUMAN_BROWSER, AI_AGENT, BOT_SCRAPER, etc.
2. **Severity** (enum) - LOW, MEDIUM, HIGH, CRITICAL
3. **SignalType** (enum) - Types de signaux comportementaux
4. **RawRequestSignals** - Données brutes de requête HTTP
5. **ClassificationResult** - Résultat de la classification des menaces
6. **BehaviorSignalExtractor** (interface) - Contrat d'extraction de caractéristiques
7. **ThreatClassifier** (interface) - Contrat de classification
8. **AIHoneypotException** - Exception de base
9. **SignalExtractionException** - Erreurs d'extraction de signaux
10. **ClassificationException** - Erreurs de classification

### Module Collector (3 classes)

1. **SessionStore** - Suivi des sessions en mémoire
2. **HttpServletSignalExtractor** - Extrait les signaux des requêtes servlet
3. **SignalCollectorFilter** - Filtre servlet interceptant toutes les requêtes

### Module Analyzer (4 classes)

1. **RuleBasedClassifier** - Détection heuristique des menaces
2. **ThreatSession** - Entité JPA pour les menaces persistées
3. **ThreatSessionRepository** - Dépôt Spring Data JPA
4. **ThreatLogService** - Service pour la journalisation des menaces en BD

### Module Honeypot

1. **AIHoneypotApplication** - Classe principale Spring Boot
2. **CanaryController** - Points de terminaison pièges (/admin, /.env, etc.)
3. **application.properties** - Configuration

## 🕷️ Points de Terminaison Pièges Canari

Les points de terminaison suivants sont des pièges qui déclenchent une classification immédiate de menace:

- `/admin`, `/wp-admin` - Pièges panneau d'administration
- `/.env`, `/.env.local` - Pièges fichiers d'environnement
- `/config`, `/configuration` - Pièges fichiers de configuration
- `/api/internal` - Piège API interne
- `/.git`, `/.svn` - Pièges contrôle de version
- `/backup`, `/*.bak` - Pièges fichiers de sauvegarde
- `/*.sql`, `/dump.sql` - Pièges dump de base de données
- `/phpmyadmin` - Piège PHPMyAdmin

## 📡 Points de Terminaison API

### API Tableau de Bord

```
GET /api/dashboard/stats                  - Statistiques générales
GET /api/dashboard/threats/recent         - Menaces récentes
GET /api/dashboard/threats/last-hours     - Menaces dans une plage horaire
GET /api/dashboard/stats/by-client-type   - Comptage par type de client
GET /api/dashboard/stats/by-severity      - Comptage par sévérité
GET /api/dashboard/stats/top-ips          - Principales IP attaquantes
GET /api/dashboard/health                 - Contrôle de santé
```

### API X Detector

```
POST /api/x-detector/analyze/manual       - Analyser un compte X (saisie manuelle)
GET  /api/x-detector/health               - Contrôle de santé
GET  /api/x-detector/info                 - Informations système
```

## 🔧 Configuration

Modifier `honeypot/src/main/resources/application.properties`:

```properties
# Base de données (H2 pour dev, PostgreSQL pour prod)
spring.datasource.url=jdbc:h2:mem:honeypotdb

# Seuil de détection des menaces
honeypot.threat.confidence-threshold=0.5

# Activer les pièges canari
honeypot.canary.enabled=true
```

## 🧪 Tests

Exécuter les tests sur tous les modules:

```bash
mvn test
```

## 📈 Signaux de Détection

Le système analyse ces signaux comportementaux:

- **Temporisation**: Intervalles de requêtes, durée de session
- **En-têtes**: Modèles User-Agent, en-têtes Accept manquants, ordre des en-têtes
- **Comportement**: Mouvement de souris, exécution JavaScript, gestion des cookies
- **Navigation**: Accès direct aux points de terminaison, referer manquant
- **Contenu**: Vitesse de soumission de formulaire, déclenchement de pièges canari
- **Réseau**: Réputation IP, géolocalisation

## 🛡️ Classification des Menaces

### Classificateur Basé sur des Règles

Utilise des heuristiques pour détecter:
- En-têtes HTTP manquants (Accept, Accept-Language)
- Chaînes User-Agent ressemblant à des bots
- Signatures d'agents IA (GPT, Claude, LangChain)
- Modèles de scanners de sécurité (Nikto, Burp)
- Accès aux pièges canari
- Modèles de requêtes suspicieusement rapides

### Classificateur Forêt d'Isolation

Utilise la détection d'anomalies statistiques pour identifier les modèles comportementaux inhabituels.

### Classificateur d'Ensemble

Combine plusieurs classificateurs pour une détection robuste.

## 🤖 X Bot Detector

Module spécialisé pour détecter les bots et agents IA sur X (Twitter) avec:

- **5 Analyseurs**: Profile, Network, Temporal, Text, Behavior
- **31 Signaux Comportementaux**: Modèles IA/LLM, métriques d'engagement, analyse temporelle
- **Mode Saisie Manuelle**: Fonctionne sans clé API Twitter
- **Prédictions Explicables**: Chaque classification inclut une explication détaillée

### Exemple de Requête X Detector

```json
{
  "username": "example_bot",
  "followersCount": 50,
  "followingCount": 5000,
  "tweetCount": 10000,
  "defaultProfileImage": true,
  "recentTweetTexts": [
    "As an AI, I'm here to assist you.",
    "Here are 5 ways to improve productivity..."
  ]
}
```

## 📝 Exemple de Détection

```json
{
  "sessionId": "ABC123",
  "timestamp": "2024-03-07T10:30:00Z",
  "clientType": "AI_AGENT",
  "confidence": 0.85,
  "severity": "HIGH",
  "isThreat": true,
  "explanation": "User-Agent d'agent IA détecté. Piège canari déclenché. En-tête Accept manquant.",
  "triggeredFeatures": {
    "canary_trap": true,
    "ai_agent_user_agent": true,
    "missing_accept_header": true
  }
}
```

## 🤝 Contribuer

Les contributions sont les bienvenues! Veuillez suivre ces étapes:

1. Fork le dépôt
2. Créer une branche de fonctionnalité
3. Apporter vos modifications
4. Ajouter des tests
5. Soumettre une pull request

## 📄 Licence

Ce projet est sous licence MIT.

## 👤 Auteur

**Dania Ciampalini**

---

⚠️ **Note**: Ceci est un système honeypot conçu pour la recherche et la surveillance de sécurité. Déployer de manière responsable et assurer la conformité avec les lois et réglementations pertinentes.

