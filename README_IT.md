# AIHoneypot - Sistema di Rilevamento Agenti AI

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-green)
![Maven](https://img.shields.io/badge/Maven-Multi--Module-blue)
![License](https://img.shields.io/badge/License-MIT-blue)

Un sofisticato sistema honeypot per rilevare e classificare agenti AI, bot e client HTTP automatizzati utilizzando fingerprinting comportamentale e machine learning.

---

## 🌍 Documentazione in Altre Lingue

- 🇬🇧 [English](README.md)
- 🇮🇹 [Italiano](README_IT.md) (Corrente)
- 🇫🇷 [Français](README_FR.md)
- 🇪🇸 [Español](README_ES.md)
- 🇩🇪 [Deutsch](README_DE.md)
- 🇨🇳 [简体中文](README_ZH.md)
- 🇯🇵 [日本語](README_JA.md)
- 🇷🇺 [Русский](README_RU.md)

---

## 🎯 Caratteristiche

- **🎨 Dashboard GUI Interattiva**: Bellissima applicazione desktop JavaFX con monitoraggio in tempo reale
  - Statistiche minacce live con auto-refresh
  - Grafici interattivi (Torta e Barre) per visualizzazione dati
  - Tabella minacce recenti con informazioni dettagliate
  - Analisi top IP attaccanti
  - Tema moderno ispirato a iOS con animazioni fluide
  
- **🚗 Simulatore di Traffico**: Genera traffico honeypot realistico per test
  - Generazione automatica traffico ogni 5-30 secondi
  - Pattern di attacco realistici (SQL injection, XSS, bot scan)
  - Ondate di traffico burst per stress testing
  - Tipi client multipli (umani, bot, agenti AI, scanner)

- **🌱 Popolamento Database**: Pre-popola il database con dati storici
  - 255+ sessioni minaccia iniziali
  - 7 giorni di pattern storici
  - Distribuzioni attacchi realistiche per severità

- **🔍 Fingerprinting Comportamentale**: Analizza i pattern delle richieste HTTP per distinguere umani da bot

- **🧠 Classificazione Multi-Livello**: 
  - Rilevamento basato su regole per pattern noti
  - Isolation Forest per rilevamento anomalie
  - Metodi ensemble che combinano più classificatori

- **🐦 Rilevamento Bot su X (Twitter)**: Modulo specializzato per rilevare bot e agenti AI sui social media
  - Analisi profilo (età, pattern username, rilevamento bio)
  - Analisi network (rapporti follower/following)
  - Pattern temporali (frequenza pubblicazione)
  - Rilevamento testo generato da AI

- **🕸️ Trappole Canary**: Endpoint esca che nessun utente legittimo dovrebbe accedere
  - `/admin`, `/wp-admin`, `/.env`, `/backup`, `/.git` e altri
  - Severità CRITICAL automatica all'accesso

- **💾 Logging Minacce in Tempo Reale**: Persiste le sessioni di minaccia su database H2/PostgreSQL

- **📊 Dashboard API REST**: Monitora e analizza le minacce rilevate tramite endpoint HTTP

- **🔄 Tracciamento Sessioni**: Correla multiple richieste dalla stessa sessione

- **🔒 Analisi Sicurezza**: Scanner sicurezza siti web con analisi a 7 livelli
  - Validazione SSL/TLS
  - Controllo header sicurezza
  - Port scanning
  - Rilevamento vulnerabilità

## 🏗️ Architettura

Questo è un progetto Maven multi-modulo con la seguente struttura:

```
AIHoneypot/
├── core/              # Modelli dominio, interfacce, eccezioni
├── collector/         # Layer raccolta segnali (filtri servlet)
├── analyzer/          # Motore classificazione minacce
├── dashboard/         # API REST e statistiche
├── gui/               # Dashboard Desktop JavaFX (NUOVO!)
├── x-detector/        # Modulo rilevamento bot X (Twitter)
└── honeypot/          # Applicazione Spring Boot principale + trappole canary + simulatore traffico
```

### Stack Tecnologico

- **Backend**: Spring Boot 3.2.2, Java 17
- **Frontend**: JavaFX 21 (GUI Desktop)
- **Database**: H2 (in-memory) / PostgreSQL (produzione)
- **ML/AI**: Isolation forest personalizzato, classificazione basata su regole
- **Grafici**: JavaFX Charts API
- **Build**: Maven Multi-Module

### Dipendenze Moduli

```
honeypot (principale)
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

## 🚀 Avvio Rapido

### Prerequisiti

- Java 17+
- Maven 3.8+
- (Opzionale) PostgreSQL per produzione

### Opzione 1: Avvio Completo (Backend + GUI) - RACCOMANDATO

Avvia tutto con un singolo comando:

```bash
./start-complete.sh
```

Questo script:
1. ✅ Controlla se il backend è in esecuzione
2. ✅ Compila il progetto se necessario
3. ✅ Avvia il backend in background
4. ✅ Aspetta che il backend sia pronto
5. ✅ Lancia la Dashboard GUI

**Nota**: Il backend viene eseguito in background. I log sono in `/tmp/aihoneypot-backend.log`

### Opzione 2: Avvio Manuale

#### Avvia Backend

```bash
cd honeypot
mvn spring-boot:run
```

L'applicazione si avvierà su `http://localhost:8080`

#### Avvia GUI (in un altro terminale)

```bash
cd gui
mvn javafx:run
```

Oppure usa lo script solo-GUI:

```bash
./start-gui-only.sh
```

### Opzione 3: Solo GUI (Backend già in esecuzione)

Se il backend è già attivo:

```bash
./start-gui-only.sh
```

Questo controlla se il backend è attivo e avvia la GUI.

### Ferma Backend

```bash
# Trova e termina il processo backend
pkill -f 'spring-boot:run'

# Oppure trova il PID e termina
lsof -ti:8080 | xargs kill -9
```

### Punti di Accesso

- **Dashboard GUI**: Si apre automaticamente quando esegui `./start-complete.sh` o `./start-gui-only.sh`
- **Swagger API**: http://localhost:8080/swagger-ui.html
- **Console H2**: http://localhost:8080/h2-console
- **Documentazione API**: http://localhost:8080/api-docs
- **Health Check**: http://localhost:8080/actuator/health

## 📊 Classi Domain Core

### Modulo Core (10 classi)

1. **ClientType** (enum) - HUMAN_BROWSER, AI_AGENT, BOT_SCRAPER, ecc.
2. **Severity** (enum) - LOW, MEDIUM, HIGH, CRITICAL
3. **SignalType** (enum) - Tipi di segnali comportamentali
4. **RawRequestSignals** - Dati richiesta HTTP raw
5. **ClassificationResult** - Output della classificazione minacce
6. **BehaviorSignalExtractor** (interfaccia) - Contratto estrazione feature
7. **ThreatClassifier** (interfaccia) - Contratto classificazione
8. **AIHoneypotException** - Eccezione base
9. **SignalExtractionException** - Errori estrazione segnali
10. **ClassificationException** - Errori classificazione

### Modulo Collector (3 classi)

1. **SessionStore** - Tracciamento sessioni in memoria
2. **HttpServletSignalExtractor** - Estrae segnali dalle richieste servlet
3. **SignalCollectorFilter** - Filtro servlet che intercetta tutte le richieste

### Modulo Analyzer (4 classi)

1. **RuleBasedClassifier** - Rilevamento minacce euristico
2. **ThreatSession** - Entità JPA per minacce persistite
3. **ThreatSessionRepository** - Repository Spring Data JPA
4. **ThreatLogService** - Servizio per logging minacce su DB

### Modulo Honeypot

1. **AIHoneypotApplication** - Classe main Spring Boot
2. **CanaryController** - Endpoint trappola (/admin, /.env, ecc.)
3. **application.properties** - Configurazione

## 🕷️ Endpoint Trappola Canary

I seguenti endpoint sono trappole che attivano la classificazione immediata della minaccia:

- `/admin`, `/wp-admin` - Trappole pannello admin
- `/.env`, `/.env.local` - Trappole file environment
- `/config`, `/configuration` - Trappole file configurazione
- `/api/internal` - Trappola API interna
- `/.git`, `/.svn` - Trappole version control
- `/backup`, `/*.bak` - Trappole file backup
- `/*.sql`, `/dump.sql` - Trappole dump database
- `/phpmyadmin` - Trappola PHPMyAdmin

## 📡 Endpoint API

### API Dashboard

```
GET /api/dashboard/stats                  - Statistiche generali
GET /api/dashboard/threats/recent         - Minacce recenti
GET /api/dashboard/threats/last-hours     - Minacce in intervallo temporale
GET /api/dashboard/stats/by-client-type   - Conteggio per tipo client
GET /api/dashboard/stats/by-severity      - Conteggio per severità
GET /api/dashboard/stats/top-ips          - IP attaccanti principali
GET /api/dashboard/health                 - Health check
```

### API X Detector

```
POST /api/x-detector/analyze/manual       - Analizza account X (input manuale)
GET  /api/x-detector/health               - Health check
GET  /api/x-detector/info                 - Informazioni sistema
```

## 🔧 Configurazione

Modifica `honeypot/src/main/resources/application.properties`:

```properties
# Database (H2 per dev, PostgreSQL per prod)
spring.datasource.url=jdbc:h2:mem:honeypotdb

# Soglia rilevamento minacce
honeypot.threat.confidence-threshold=0.5

# Abilita trappole canary
honeypot.canary.enabled=true
```

## 🧪 Testing

Esegui i test su tutti i moduli:

```bash
mvn test
```

## 📈 Segnali di Rilevamento

Il sistema analizza questi segnali comportamentali:

- **Temporali**: Intervalli richieste, durata sessione
- **Header**: Pattern User-Agent, header Accept mancanti, ordine header
- **Comportamentali**: Movimento mouse, esecuzione JavaScript, gestione cookie
- **Navigazione**: Accesso diretto endpoint, referer mancante
- **Contenuto**: Velocità invio form, attivazione trappole canary
- **Network**: Reputazione IP, geolocalizzazione

## 🛡️ Classificazione Minacce

### Classificatore Basato su Regole

Utilizza euristiche per rilevare:
- Header HTTP mancanti (Accept, Accept-Language)
- Stringhe User-Agent simili a bot
- Firme agenti AI (GPT, Claude, LangChain)
- Pattern scanner sicurezza (Nikto, Burp)
- Accesso trappole canary
- Pattern richieste sospettosamente veloci

### Classificatore Isolation Forest

Utilizza rilevamento anomalie statistiche per identificare pattern comportamentali inusuali.

### Classificatore Ensemble

Combina più classificatori per un rilevamento robusto.

## 🤖 X Bot Detector

Modulo specializzato per rilevare bot e agenti AI su X (Twitter) con:

- **5 Analizzatori**: Profile, Network, Temporal, Text, Behavior
- **31 Segnali Comportamentali**: Pattern AI/LLM, metriche engagement, analisi temporale
- **Modalità Input Manuale**: Funziona senza API key Twitter
- **Predizioni Spiegabili**: Ogni classificazione include spiegazione dettagliata

### Esempio Richiesta X Detector

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

## 📝 Esempio Rilevamento

```json
{
  "sessionId": "ABC123",
  "timestamp": "2024-03-07T10:30:00Z",
  "clientType": "AI_AGENT",
  "confidence": 0.85,
  "severity": "HIGH",
  "isThreat": true,
  "explanation": "Rilevato User-Agent agente AI. Trappola canary attivata. Header Accept mancante.",
  "triggeredFeatures": {
    "canary_trap": true,
    "ai_agent_user_agent": true,
    "missing_accept_header": true
  }
}
```

## 🤝 Contribuire

I contributi sono benvenuti! Segui questi passaggi:

1. Fai fork del repository
2. Crea un branch per la feature
3. Apporta le tue modifiche
4. Aggiungi test
5. Invia una pull request

## 📄 Licenza

Questo progetto è rilasciato sotto licenza MIT.

## 👤 Autore

**Dania Ciampalini**

---

⚠️ **Nota**: Questo è un sistema honeypot progettato per ricerca e monitoraggio della sicurezza. Distribuisci responsabilmente e assicura la conformità con le leggi e i regolamenti pertinenti.

