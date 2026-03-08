# AIHoneypot - KI-Agenten-Erkennungssystem

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-green)
![Maven](https://img.shields.io/badge/Maven-Multi--Module-blue)
![License](https://img.shields.io/badge/License-MIT-blue)

Ein ausgeklügeltes Honeypot-System zur Erkennung und Klassifizierung von KI-Agenten, Bots und automatisierten HTTP-Clients mittels Verhaltensfingerprinting und maschinellem Lernen.

---

## 🌍 Dokumentation in Anderen Sprachen

- 🇬🇧 [English](README.md)
- 🇮🇹 [Italiano](README_IT.md)
- 🇫🇷 [Français](README_FR.md)
- 🇪🇸 [Español](README_ES.md)
- 🇩🇪 [Deutsch](README_DE.md) (Aktuell)
- 🇨🇳 [简体中文](README_ZH.md)
- 🇯🇵 [日本語](README_JA.md)
- 🇷🇺 [Русский](README_RU.md)

---

## 🎯 Funktionen

- **🎨 Interaktives GUI-Dashboard**: Schöne JavaFX-Desktop-Anwendung mit Echtzeit-Überwachung
  - Live-Bedrohungsstatistiken mit automatischer Aktualisierung
  - Interaktive Diagramme (Kreis- und Balkendiagramme) zur Datenvisualisierung
  - Tabelle der aktuellen Bedrohungen mit detaillierten Informationen
  - Analyse der wichtigsten angreifenden IPs
  - Modernes iOS-inspiriertes Design mit flüssigen Animationen
  
- **🚗 Traffic-Simulator**: Generiert realistischen Honeypot-Traffic für Tests
  - Automatische Traffic-Generierung alle 5-30 Sekunden
  - Realistische Angriffsmuster (SQL-Injection, XSS, Bot-Scans)
  - Burst-Traffic-Wellen für Stresstests
  - Mehrere Client-Typen (Menschen, Bots, KI-Agenten, Scanner)

- **🌱 Datenbank-Seeding**: Füllt die Datenbank mit historischen Daten vor
  - 255+ initiale Bedrohungssitzungen
  - 7 Tage historische Muster
  - Realistische Angriffsverteilungen nach Schweregrad

- **🔍 Verhaltens-Fingerprinting**: Analysiert HTTP-Anfragemuster, um Menschen von Bots zu unterscheiden

- **🧠 Mehrstufige Klassifizierung**: 
  - Regelbasierte Erkennung für bekannte Muster
  - Isolation Forest für Anomalieerkennung
  - Ensemble-Methoden, die mehrere Klassifikatoren kombinieren

- **🐦 X (Twitter) Bot-Erkennung**: Spezialisiertes Modul zur Erkennung von Bots und KI-Agenten in sozialen Medien
  - Profilanalyse (Alter, Benutzernamen-Muster, Bio-Erkennung)
  - Netzwerkanalyse (Follower/Following-Verhältnisse)
  - Zeitliche Muster (Veröffentlichungsfrequenz)
  - Erkennung von KI-generiertem Text

- **🕸️ Canary-Fallen**: Köder-Endpunkte, auf die kein legitimer Benutzer zugreifen sollte
  - `/admin`, `/wp-admin`, `/.env`, `/backup`, `/.git` und mehr
  - Automatischer KRITISCHER Schweregrad beim Zugriff

- **💾 Echtzeit-Bedrohungsprotokollierung**: Speichert Bedrohungssitzungen in H2/PostgreSQL-Datenbank

- **📊 REST-API-Dashboard**: Überwacht und analysiert erkannte Bedrohungen über HTTP-Endpunkte

- **🔄 Sitzungsverfolgung**: Korreliert mehrere Anfragen derselben Sitzung

- **🔒 Sicherheitsanalyse**: Website-Sicherheitsscanner mit 7-stufiger Analyse
  - SSL/TLS-Validierung
  - Überprüfung von Sicherheits-Headern
  - Port-Scanning
  - Schwachstellenerkennung

## 🏗️ Architektur

Dies ist ein Maven-Multi-Modul-Projekt mit folgender Struktur:

```
AIHoneypot/
├── core/              # Domänenmodelle, Schnittstellen, Ausnahmen
├── collector/         # Signalerfassungsschicht (Servlet-Filter)
├── analyzer/          # Bedrohungsklassifizierungs-Engine
├── dashboard/         # REST-API und Statistiken
├── x-detector/        # X (Twitter) Bot-Erkennungsmodul
└── honeypot/          # Haupt-Spring-Boot-Anwendung + Canary-Fallen
```

### Modulabhängigkeiten

```
honeypot (Haupt)
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

## 🚀 Schnellstart

### Voraussetzungen

- Java 17+
- Maven 3.8+
- (Optional) PostgreSQL für Produktion

### Build

```bash
mvn clean install
```

### Ausführung

```bash
cd honeypot
mvn spring-boot:run
```

Die Anwendung startet auf `http://localhost:8080`

### Dashboard-Zugriff

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2-Konsole**: http://localhost:8080/h2-console
- **API-Dokumentation**: http://localhost:8080/api-docs

## 📊 Core-Domänenklassen

### Core-Modul (10 Klassen)

1. **ClientType** (enum) - HUMAN_BROWSER, AI_AGENT, BOT_SCRAPER, usw.
2. **Severity** (enum) - LOW, MEDIUM, HIGH, CRITICAL
3. **SignalType** (enum) - Arten von Verhaltenssignalen
4. **RawRequestSignals** - Rohe HTTP-Anfragedaten
5. **ClassificationResult** - Ergebnis der Bedrohungsklassifizierung
6. **BehaviorSignalExtractor** (Schnittstelle) - Merkmalextraktionsvertrag
7. **ThreatClassifier** (Schnittstelle) - Klassifizierungsvertrag
8. **AIHoneypotException** - Basisausnahme
9. **SignalExtractionException** - Signalextraktionsfehler
10. **ClassificationException** - Klassifizierungsfehler

### Collector-Modul (3 Klassen)

1. **SessionStore** - Sitzungsverfolgung im Speicher
2. **HttpServletSignalExtractor** - Extrahiert Signale aus Servlet-Anfragen
3. **SignalCollectorFilter** - Servlet-Filter, der alle Anfragen abfängt

### Analyzer-Modul (4 Klassen)

1. **RuleBasedClassifier** - Heuristische Bedrohungserkennung
2. **ThreatSession** - JPA-Entität für persistierte Bedrohungen
3. **ThreatSessionRepository** - Spring Data JPA Repository
4. **ThreatLogService** - Service für Bedrohungsprotokollierung in DB

### Honeypot-Modul

1. **AIHoneypotApplication** - Spring Boot Hauptklasse
2. **CanaryController** - Fallen-Endpunkte (/admin, /.env, usw.)
3. **application.properties** - Konfiguration

## 🕷️ Canary-Fallen-Endpunkte

Die folgenden Endpunkte sind Fallen, die sofortige Bedrohungsklassifizierung auslösen:

- `/admin`, `/wp-admin` - Admin-Panel-Fallen
- `/.env`, `/.env.local` - Umgebungsdatei-Fallen
- `/config`, `/configuration` - Konfigurationsdatei-Fallen
- `/api/internal` - Interne API-Falle
- `/.git`, `/.svn` - Versionskontroll-Fallen
- `/backup`, `/*.bak` - Backup-Datei-Fallen
- `/*.sql`, `/dump.sql` - Datenbank-Dump-Fallen
- `/phpmyadmin` - PHPMyAdmin-Falle

## 📡 API-Endpunkte

### Dashboard-API

```
GET /api/dashboard/stats                  - Gesamtstatistiken
GET /api/dashboard/threats/recent         - Neueste Bedrohungen
GET /api/dashboard/threats/last-hours     - Bedrohungen im Zeitbereich
GET /api/dashboard/stats/by-client-type   - Zählung nach Client-Typ
GET /api/dashboard/stats/by-severity      - Zählung nach Schweregrad
GET /api/dashboard/stats/top-ips          - Top angreifende IPs
GET /api/dashboard/health                 - Gesundheitsprüfung
```

### X Detector API

```
POST /api/x-detector/analyze/manual       - X-Konto analysieren (manuelle Eingabe)
GET  /api/x-detector/health               - Gesundheitsprüfung
GET  /api/x-detector/info                 - Systeminformationen
```

## 🔧 Konfiguration

Bearbeiten Sie `honeypot/src/main/resources/application.properties`:

```properties
# Datenbank (H2 für Dev, PostgreSQL für Prod)
spring.datasource.url=jdbc:h2:mem:honeypotdb

# Bedrohungserkennungsschwelle
honeypot.threat.confidence-threshold=0.5

# Canary-Fallen aktivieren
honeypot.canary.enabled=true
```

## 🧪 Tests

Tests über alle Module ausführen:

```bash
mvn test
```

## 📈 Erkennungssignale

Das System analysiert diese Verhaltenssignale:

- **Timing**: Anfrageintervalle, Sitzungsdauer
- **Header**: User-Agent-Muster, fehlende Accept-Header, Header-Reihenfolge
- **Verhalten**: Mausbewegung, JavaScript-Ausführung, Cookie-Handling
- **Navigation**: Direkter Endpunkt-Zugriff, fehlender Referer
- **Inhalt**: Formularübermittlungsgeschwindigkeit, Canary-Fallen-Auslösung
- **Netzwerk**: IP-Reputation, Geolokalisierung

## 🛡️ Bedrohungsklassifizierung

### Regelbasierter Klassifizierer

Verwendet Heuristiken zur Erkennung von:
- Fehlende HTTP-Header (Accept, Accept-Language)
- Bot-ähnliche User-Agent-Strings
- KI-Agenten-Signaturen (GPT, Claude, LangChain)
- Sicherheitsscanner-Muster (Nikto, Burp)
- Canary-Fallen-Zugriff
- Verdächtig schnelle Anfragemuster

### Isolation Forest Klassifizierer

Verwendet statistische Anomalieerkennung zur Identifizierung ungewöhnlicher Verhaltensmuster.

### Ensemble-Klassifizierer

Kombiniert mehrere Klassifizierer für robuste Erkennung.

## 🤖 X Bot Detector

Spezialisiertes Modul zur Erkennung von Bots und KI-Agenten auf X (Twitter) mit:

- **5 Analysatoren**: Profile, Network, Temporal, Text, Behavior
- **31 Verhaltenssignale**: KI/LLM-Muster, Engagement-Metriken, temporale Analyse
- **Manueller Eingabemodus**: Funktioniert ohne Twitter-API-Schlüssel
- **Erklärbare Vorhersagen**: Jede Klassifizierung enthält detaillierte Erklärung

### Beispiel X Detector Anfrage

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

## 📝 Erkennungsbeispiel

```json
{
  "sessionId": "ABC123",
  "timestamp": "2024-03-07T10:30:00Z",
  "clientType": "AI_AGENT",
  "confidence": 0.85,
  "severity": "HIGH",
  "isThreat": true,
  "explanation": "KI-Agenten-User-Agent erkannt. Canary-Falle ausgelöst. Accept-Header fehlt.",
  "triggeredFeatures": {
    "canary_trap": true,
    "ai_agent_user_agent": true,
    "missing_accept_header": true
  }
}
```

## 🤝 Mitwirken

Beiträge sind willkommen! Bitte befolgen Sie diese Schritte:

1. Repository forken
2. Feature-Branch erstellen
3. Änderungen vornehmen
4. Tests hinzufügen
5. Pull-Request einreichen

## 📄 Lizenz

Dieses Projekt ist unter der MIT-Lizenz lizenziert.

## 👤 Autor

**Dania Ciampalini**

---

⚠️ **Hinweis**: Dies ist ein Honeypot-System für Forschung und Sicherheitsüberwachung. Verantwortungsvoll einsetzen und Einhaltung relevanter Gesetze und Vorschriften sicherstellen.

