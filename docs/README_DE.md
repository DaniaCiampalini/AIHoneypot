# AIHoneypot - KI-Agenten-Erkennungssystem

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-green)
![Maven](https://img.shields.io/badge/Maven-Multi--Module-blue)
![License](https://img.shields.io/badge/License-MIT-blue)

Ein ausgeklügeltes Honeypot-System zur Erkennung und Klassifizierung von KI-Agenten, Bots und automatisierten HTTP-Clients mittels Verhaltensfingerprinting und regelbasierten Heuristiken.

---

## Dokumentation in anderen Sprachen

- [English](../README.md)
- [Italiano](README_IT.md)
- [Español](README_ES.md)
- [Deutsch](README_DE.md) (Aktuell)

---

## Funktionen

- **Interaktives GUI-Dashboard**: Schöne JavaFX-Desktop-Anwendung mit Echtzeit-Überwachung
  - Live-Bedrohungsstatistiken mit automatischer Aktualisierung
  - Interaktive Diagramme (Kreis- und Balkendiagramme) zur Datenvisualisierung
  - Tabelle der aktuellen Bedrohungen mit detaillierten Informationen
  - Analyse der wichtigsten angreifenden IPs
  - Modernes iOS-inspiriertes Design mit flüssigen Animationen
  
- **Traffic-Simulator**: Generiert realistischen Honeypot-Traffic für Tests
  - Automatische Traffic-Generierung alle 5 Sekunden
  - Angriffssimulationen alle 30 Sekunden
  - Burst-Traffic-Wellen alle 2 Minuten für Stresstests
  - Mehrere Client-Typen (Menschen, Bots, KI-Agenten, Scanner)

- **Datenbank-Seeding**: Füllt die Datenbank mit historischen Daten vor
  - ~270 initiale Bedrohungssitzungen
  - 7 Tage historische Muster
  - Realistische Angriffsverteilungen nach Schweregrad (einschließlich SQL-Injection und Bot-Scan-Spitzen)

- **Verhaltens-Fingerprinting**: Analysiert HTTP-Anfragemuster, um Menschen von Bots zu unterscheiden

## Bedrohungsklassifizierung
- **Ebene 1: Regelbasierter Klassifizierer**: Heuristische Erkennung basierend auf Headern, Timing und Canary-Zugriff (Entscheidbar durch Konstruktion).
- **Ebene 2: Isolation Forest Anomalieerkennung**: Unüberwachtes maschinelles Lernen zur Erkennung unbekannter Angriffsmuster und Verteilungsverschiebungen (Fallstudie der Abschlussarbeit).
- **Ebene 3: Ensemble-Erkennung**: Integration von regelbasierten und ML-Ansätzen zur Überwindung individueller theoretischer Grenzen (No-Free-Lunch-Theorem).

---

## Abschlussarbeit: Theoretische Grenzen von ML-Systemen für die Sicherheit
Dieses Projekt ist als Fallstudie für eine Abschlussarbeit über die Grenzen der automatisierten Erkennung strukturiert.
- **Testszenarien**:
  1. **Bekannte Muster**: Wirksamkeit des regelbasierten Klassifizierers.
  2. **Unbekannte Anomalien**: Wirksamkeit des Isolation Forest bei Zero-Day-Angriffen.
  3. **Grenzfall-Traffic**: Analyse von falsch-positiven/negativen Ergebnissen und Grenzen des PAC-Lernens.
- **Weka-Integration**: Verwendung des Pakets isolationForest für die statistische Traffic-Analyse.

- **X (Twitter) Bot-Erkennung**: Spezialisiertes Modul zur Erkennung von Bots und KI-Agenten in sozialen Medien
  - Profilanalyse (Alter, Benutzernamen-Muster, Bio-Erkennung)
  - Netzwerkanalyse (Follower/Following-Verhältnisse)
  - Zeitliche Muster (Veröffentlichungsfrequenz)
  - Erkennung von KI-generiertem Text
  - Erklärbares Scoring und Empfehlungen

- **Canary-Fallen**: Köder-Endpunkte, auf die kein legitimer Benutzer zugreifen sollte
  - /admin, /wp-admin
  - /.env
  - /config
  - /api/internal
  - /.git
  - /backup
  - Automatischer KRITISCHER Schweregrad beim Zugriff

- **Echtzeit-Bedrohungsprotokollierung**: Speichert Bedrohungssitzungen in H2/PostgreSQL-Datenbank

- **REST-API-Dashboard**: Überwacht und analysiert erkannte Bedrohungen über HTTP-Endpunkte
  - CSV-Export: Datenexport für experimentelle Analysen und Auswertungen der Abschlussarbeit

- **Sitzungsverfolgung**: Korreliert mehrere Anfragen derselben Sitzung
  - Fortgeschrittenes Feature-Engineering: Verfolgung von Anfrageraten und durchschnittlichem Timing zwischen Anfragen

- **Website-Sicherheitsanalyse (GUI)**:
  - SSL/HTTPS-Prüfungen
  - Überprüfung von Sicherheits-Headern
  - Heuristische Schwachstellen- und Risikobewertung

---

## Architektur

Dies ist ein Maven-Multi-Modul-Projekt mit folgender Struktur:

```
AIHoneypot/
├── docs/              # Übersetzte README-Dateien
├── scripts/           # Startup-Shell-Skripte
├── core/              # Domänenmodelle, Schnittstellen, Ausnahmen
├── collector/         # Signalerfassungsschicht (Servlet-Filter)
├── analyzer/          # Bedrohungsklassifizierungs-Engine
├── dashboard/         # REST-API und Statistiken
├── gui/               # JavaFX-Desktop-Dashboard
├── x-detector         # X (Twitter) Bot-Erkennungsmodul
└── honeypot/          # Haupt-Spring-Boot-Anwendung + Canary-Fallen + Traffic-Simulator
```

### Technologie-Stack

- **Backend**: Spring Boot 3.2.2, Java 17
- **Frontend**: JavaFX 21 (Desktop-GUI)
- **Datenbank**: H2 (In-Memory) / PostgreSQL (Produktion)
- **ML/KI**: Weka Isolation Forest, regelbasierte Heuristiken, Ensemble-Methoden
- **Diagramme**: JavaFX Charts API
- **Build**: Maven Multi-Module

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

---

## Schnellstart

### Voraussetzungen

- Java 17+
- Maven 3.8+
- (Optional) PostgreSQL für Produktion

### Option 1: Vollständiger Start (Backend + GUI) - EMPFOHLEN

Starten Sie alles mit einem einzigen Befehl:

```bash
./scripts/start-complete.sh
```

Dieses Skript wird:
1. Prüfen, ob das Backend läuft  
2. Das Projekt bei Bedarf bauen  
3. Das Backend im Hintergrund starten  
4. Warten, bis das Backend bereit ist  
5. Das GUI-Dashboard starten  

**Hinweis**: Das Backend läuft im Hintergrund. Protokolle befinden sich in /tmp/aihoneypot-backend.log

### Option 2: Manueller Start

#### Backend starten

```bash
cd honeypot
mvn spring-boot:run
```

Die Anwendung startet auf http://localhost:8080

#### GUI starten (in einem anderen Terminal)

```bash
cd gui
mvn javafx:run
```

Oder verwenden Sie das Nur-GUI-Skript:

```bash
./scripts/start-gui-only.sh
```

### Option 3: Nur GUI (Backend läuft bereits)

Wenn das Backend bereits läuft:

```bash
./scripts/start-gui-only.sh
```

Dies prüft, ob das Backend aktiv ist, und startet die GUI.

### Backend stoppen

```bash
# Backend-Prozess finden und beenden
pkill -f 'spring-boot:run'

# Oder PID finden und beenden
lsof -ti:8080 | xargs kill -9
```

### Zugriffspunkte

- **GUI-Dashboard**: Öffnet sich automatisch beim Ausführen von ./scripts/start-complete.sh oder ./scripts/start-gui-only.sh
- **API Swagger**: http://localhost:8080/swagger-ui.html
- **H2-Konsole**: http://localhost:8080/h2-console
- **API-Dokumentation**: http://localhost:8080/api-docs
- **Gesundheitsprüfung**: http://localhost:8080/actuator/health

---

## Core-Domänenklassen

### Core-Modul (10 Klassen)

1. **ClientType** (enum) - HUMAN_BROWSER, AI_AGENT, BOT_SCRAPER, SEARCH_ENGINE, SECURITY_SCANNER, UNKNOWN  
2. **Severity** (enum) - LOW, MEDIUM, HIGH, CRITICAL  
3. **SignalType** (enum) - Arten von Verhaltenssignalen  
4. **RawRequestSignals** - Rohe HTTP-Anfragedaten  
5. **ClassificationResult** - Ergebnis der Bedrohungsklassifizierung  
6. **BehaviorSignalExtractor** (Schnittstelle) - Merkmalextraktionsvertrag  
7. **ThreatClassifier** (Schnittstelle) - Klassifizierungsvertrag  
8. **AIHoneypotException** - Basisausnahme  
9. **SignalExtractionException** - Signalextraktionsfehler  
10. **ClassificationException** - Klassifizierungsfehler  

### Collector-Modul (4 Klassen)

1. **SessionStore** - Sitzungsverfolgung im Speicher  
2. **HttpServletSignalExtractor** - Extrahiert Signale aus Servlet-Anfragen
3. **SignalCollectorFilter** - Servlet-Filter, der alle Anfragen abfängt
4. **IsolationForestAnomalyDetector** - Unüberwachte Anomalieerkennung mit Weka

### Analyzer-Modul (5 Klassen)

1. **RuleBasedClassifier** - Heuristische Bedrohungserkennung  
2. **EnsembleClassifier** - Aggregierter Klassifizierer (Ebene 3)
3. **ThreatSession** - JPA-Entität für persistierte Bedrohungen  
4. **ThreatSessionRepository** - Spring Data JPA Repository  
5. **ThreatLogService** - Service zur Protokollierung von Bedrohungen in der DB

### Honeypot-Modul

1. **AIHoneypotApplication** - Spring Boot Hauptklasse  
2. **CanaryController** - Fallen-Endpunkte  
3. **TrafficSimulator** - Realistischer Traffic und Angriffssimulation
4. **DatabaseSeeder** - Initiale Datenbefüllung
5. **application.properties** - Konfiguration  

---

## Canary-Fallen-Endpunkte

Die folgenden Endpunkte sind Fallen, die eine sofortige Bedrohungsklassifizierung auslösen:

- /admin, /wp-admin  
- /.env  
- /config  
- /api/internal  
- /.git  
- /backup  

---

## API-Endpunkte

### Dashboard-API

```
GET /api/dashboard/stats                  - Gesamtstatistiken
GET /api/dashboard/threats/recent         - Neueste Bedrohungen
GET /api/dashboard/threats/last-hours     - Bedrohungen im Zeitbereich
GET /api/dashboard/stats/by-client-type   - Zählung nach Client-Typ
GET /api/dashboard/stats/by-severity      - Zählung nach Schweregrad
GET /api/dashboard/stats/top-ips          - Top angreifende IPs
GET /api/dashboard/export/csv             - Bedrohungen nach CSV exportieren
GET /api/dashboard/health                 - Gesundheitsprüfung
```

---

## Konfiguration

Bearbeiten Sie honeypot/src/main/resources/application.properties:

```properties
# Datenbank (H2 für Dev, PostgreSQL für Prod)
spring.datasource.url=jdbc:h2:mem:honeypotdb

# Bedrohungserkennungsschwelle
honeypot.threat.confidence-threshold=0.5

# Canary-Fallen aktivieren
honeypot.canary.enabled=true
```

---

## Tests

Tests über alle Module ausführen:

```bash
mvn test
```

---

## Erkennungssignale

Das System analysiert diese Verhaltenssignale:

- **Timing**: Anfrageintervalle, Sitzungsdauer, durchschnittliches Timing zwischen Anfragen  
- **Header**: User-Agent-Muster, fehlende Accept-Header, Header-Reihenfolge  
- **Verhalten**: JavaScript-Erkennung, Cookie-Handling (falls verfügbar)  
- **Navigation**: Direkter Endpunkt-Zugriff, fehlender Referer, Pfadtiefe  
- **Inhalt**: Canary-Fallen-Auslösung, Query-Komplexität, Verhältnis verdächtiger Zeichen  
- **Netzwerk**: IP-Analyse, Anfragerate pro Minute

---

## Bedrohungsklassifizierung

### Regelbasierter Klassifizierer

Verwendet Heuristiken zur Erkennung von:
- Fehlenden HTTP-Headern (Accept, Accept-Language)
- Bot-ähnlichen User-Agent-Strings
- KI-Agenten-Signaturen (GPT, Claude, LangChain)
- Sicherheitsscanner-Mustern (Nikto, Burp)
- Canary-Fallen-Zugriff
- Verdächtig schnellen Anfragemustern

### Isolation Forest Anomalieerkennung

Unüberwachte Machine-Learning-Schicht, die:
- Normale Traffic-Muster lernt
- Abweichungen (Anomalien) ohne Vorabwissen über Angriffsarten erkennt
- 12 Verhaltensmerkmale verwendet, einschließlich Timing-Entropie und URI-Struktur

### Ensemble-Klassifizierer

Aggregiert mehrere Erkennungsschichten:
- Kombiniert Ebene 1 (Regelbasiert) und Ebene 2 (ML)
- Löst Diskrepanzen zwischen Klassifizierern
- Implementiert theoretische Minderungen für das No-Free-Lunch-Theorem

---

## X Bot Detector

Spezialisiertes Modul zur Erkennung von Bots und KI-Agenten auf X (Twitter) mit fortgeschrittener Verhaltensanalyse.

### Funktionen

- **5 unabhängige Analysatoren**: Profil, Netzwerk, Temporal, Text, Verhalten  
- **Erklärbare Vorhersagen**: Jede Klassifizierung enthält eine detaillierte Erklärung und aktivierte Signale  
- **Konfigurierbare Gewichte**: Jeder Analysator hat ein einstellbares Gewicht in der Endbewertung  
- **Manueller Eingabemodus**: Funktioniert ohne Twitter-API-Schlüssel  

### Aufschlüsselung der Analysatoren

| Analysator | Gewicht | Schlüssel-Signale |
|------------|---------|-------------------|
| **ProfileAnalyzer** | 20% | Kontoalter, Benutzernamen-Muster, KI-Schlüsselwörter in der Bio, Standardbild |
| **NetworkAnalyzer** | 25% | Follower/Following-Verhältnis, Follow-Spam, Engagement-Anomalien |
| **TemporalAnalyzer** | 25% | Posting-Frequenz, regelmäßige Intervalle (CV), Burst-Posting |
| **TextAnalyzer** | 30% | KI/LLM-Muster, Textwiederholung, Automatisierungs-Clients, Vokabular |
| **BehaviorAnalyzer** | 20% | Retweet-Verhältnis, keine Antworten, API-Clients, Sprachuniformität |

### Klassifizierungsstufen

| Score-Bereich | Klassifizierung | Aktion |
|---------------|-----------------|---------|
| < 0.3 | **WAHRSCHEINLICH MENSCHLICH** | Erlauben |
| 0.3 - 0.6 | **UNSICHER** | Überwachen |
| 0.6 - 0.8 | **WAHRSCHEINLICH BOT** | Überprüfen |
| > 0.8 | **BESTÄTIGTER BOT** | Blockieren |

---

## Erkennungsbeispiel

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

---

## Roadmap

Folgende Punkte sind für zukünftige Iterationen geplant:

- **Erweiterte Abdeckung von Canary-Endpunkten** über die aktuelle Core-Liste hinaus
- **Umfangreichere Verhaltenssignale** (Mausbewegung, fortgeschrittenes JS-Fingerprinting)
- **Erweiterte Sicherheitsanalysetiefe** mit mehrstufigem Scoring

---

## Mitwirken

Beiträge sind willkommen! Bitte befolgen Sie diese Schritte:

1. Forken Sie das Repository  
2. Erstellen Sie einen Feature-Branch  
3. Nehmen Sie Ihre Änderungen vor  
4. Fügen Sie Tests hinzu  
5. Reichen Sie einen Pull-Request ein  

---

## Lizenz

Dieses Projekt ist unter der MIT-Lizenz lizenziert.

---

## Autor

**Dania Ciampalini**

---

**Hinweis**: Dies ist ein Honeypot-System, das für Forschung und Sicherheitsüberwachung entwickelt wurde. Setzen Sie es verantwortungsbewusst ein und stellen Sie die Einhaltung relevanter Gesetze und Vorschriften sicher.
