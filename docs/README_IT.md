# AIHoneypot - Sistema di Rilevamento Agenti AI

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-green)
![Maven](https://img.shields.io/badge/Maven-Multi--Module-blue)
![License](https://img.shields.io/badge/License-MIT-blue)

Un sofisticato sistema honeypot per rilevare e classificare agenti AI, bot e client HTTP automatizzati utilizzando fingerprinting comportamentale ed euristiche basate su regole.

---

## Documentazione in Altre Lingue

- [English](../README.md)
- [Italiano](README_IT.md) (Corrente)
- [Español](README_ES.md)
- [Deutsch](README_DE.md)

---

## Caratteristiche

- **Dashboard GUI Interattiva**: Bellissima applicazione desktop JavaFX con monitoraggio in tempo reale
  - Statistiche minacce live con auto-refresh
  - Grafici interattivi (Torta e Barre) per visualizzazione dati
  - Tabella minacce recenti con informazioni dettagliate
  - Analisi top IP attaccanti
  - Tema moderno ispirato a iOS con animazioni fluide
  
- **Simulatore di Traffico**: Genera traffico honeypot realistico per test
  - Generazione automatica traffico ogni 5 secondi
  - Simulazioni di attacco ogni 30 secondi
  - Ondate di traffico burst ogni 2 minuti per stress testing
  - Tipi client multipli (umani, bot, agenti AI, scanner)

- **Popolamento Database**: Pre-popola il database con dati storici
  - ~270 sessioni minaccia iniziali
  - 7 giorni di pattern storici
  - Distribuzioni attacchi realistiche per severità (inclusi SQL injection e picchi di bot scan)

- **Fingerprinting Comportamentale**: Analizza i pattern delle richieste HTTP per distinguere umani da bot

## Classificazione Minacce
- **Layer 1: Rule-Based Classifier**: Rilevamento euristico basato su header, timing e accesso a canary (Decidibile per costruzione).
- **Layer 2: Isolation Forest Anomaly Detection**: ML non supervisionato per rilevare pattern di attacco ignoti e shift della distribuzione (Caso di studio tesi).
- **Layer 3: Ensemble Detection**: Integrazione di approcci basati su regole e ML per superare i limiti teorici individuali (Teorema No Free Lunch).

---

## Tesi: Limiti teorici dei sistemi ML per la security
Questo progetto è strutturato come caso di studio per la tesi sui limiti della rilevazione automatica.
- **Scenari di Test**:
  1. **Pattern Noti**: Efficacia del Rule-Based Classifier.
  2. **Anomalie Ignoto**: Efficacia dell'Isolation Forest su zero-day.
  3. **Traffico Borderline**: Analisi dei falsi positivi/negativi e limiti del PAC-learning.
- **Integrazione Weka**: Utilizzo del package isolationForest per l'analisi statistica del traffico.

- **Rilevamento Bot su X (Twitter)**: Modulo specializzato per rilevare bot e agenti AI sui social media
  - Analisi profilo (età, pattern username, rilevamento bio)
  - Analisi network (rapporti follower/following)
  - Pattern temporali (frequenza pubblicazione)
  - Rilevamento testo generato da AI
  - Scoring spiegabile e raccomandazioni

- **Trappole Canary**: Endpoint esca che nessun utente legittimo dovrebbe accedere
  - /admin, /wp-admin
  - /.env
  - /config
  - /api/internal
  - /.git
  - /backup
  - Severità CRITICAL automatica all'accesso

- **Logging Minacce in Tempo Reale**: Persiste le sessioni di minaccia su database H2/PostgreSQL

- **Dashboard API REST**: Monitora e analizza le minacce rilevate tramite endpoint HTTP
  - Export CSV: Esportazione dati per analisi sperimentale e valutazione tesi

- **Tracciamento Sessioni**: Correla multiple richieste dalla stessa sessione
  - Feature Engineering Avanzato: Tracciamento dei tassi di richiesta e timing medio tra le richieste

- **Analisi Sicurezza Sito Web (GUI)**:
  - Controlli SSL/HTTPS
  - Revisione header di sicurezza
  - Scoring euristico di vulnerabilità e rischio

---

## Architettura

Questo è un progetto Maven multi-modulo con la seguente struttura:

```
AIHoneypot/
├── docs/              # File README tradotti
├── scripts/           # Script shell di avvio
├── core/              # Modelli dominio, interfacce, eccezioni
├── collector/         # Layer raccolta segnali (filtri servlet)
├── analyzer/          # Motore classificazione minacce
├── dashboard/         # API REST e statistiche
├── gui/               # Dashboard Desktop JavaFX
├── x-detector         # Modulo rilevamento bot X (Twitter)
└── honeypot/          # Applicazione Spring Boot principale + trappole canary + simulatore traffico
```

### Stack Tecnologico

- **Backend**: Spring Boot 3.2.2, Java 17
- **Frontend**: JavaFX 21 (GUI Desktop)
- **Database**: H2 (in-memory) / PostgreSQL (produzione)
- **ML/AI**: Weka Isolation Forest, Euristiche basate su regole, Metodi Ensemble
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

---

## Avvio Rapido

### Prerequisiti

- Java 17+
- Maven 3.8+
- (Opzionale) PostgreSQL per produzione

### Opzione 1: Avvio Completo (Backend + GUI) - RACCOMANDATO

Avvia tutto con un singolo comando:

```bash
./scripts/start-complete.sh
```

Questo script:
1. Controlla se il backend è in esecuzione
2. Compila il progetto se necessario
3. Avvia il backend in background
4. Aspetta che il backend sia pronto
5. Lancia la Dashboard GUI

**Nota**: Il backend viene eseguito in background. I log sono in /tmp/aihoneypot-backend.log

### Opzione 2: Avvio Manuale

#### Avvia Backend

```bash
cd honeypot
mvn spring-boot:run
```

L'applicazione si avvierà su http://localhost:8080

#### Avvia GUI (in un altro terminale)

```bash
cd gui
mvn javafx:run
```

Oppure usa lo script solo-GUI:

```bash
./scripts/start-gui-only.sh
```

### Opzione 3: Solo GUI (Backend già in esecuzione)

Se il backend è già attivo:

```bash
./scripts/start-gui-only.sh
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

- **Dashboard GUI**: Si apre automaticamente quando esegui ./scripts/start-complete.sh o ./scripts/start-gui-only.sh
- **Swagger API**: http://localhost:8080/swagger-ui.html
- **Console H2**: http://localhost:8080/h2-console
- **Documentazione API**: http://localhost:8080/api-docs
- **Health Check**: http://localhost:8080/actuator/health

---

## Classi Domain Core

### Modulo Core (10 classi)

1. **ClientType** (enum) - HUMAN_BROWSER, AI_AGENT, BOT_SCRAPER, SEARCH_ENGINE, SECURITY_SCANNER, UNKNOWN
2. **Severity** (enum) - LOW, MEDIUM, HIGH, CRITICAL
3. **SignalType** (enum) - Tipi di segnali comportamentali
4. **RawRequestSignals** - Dati richiesta HTTP raw
5. **ClassificationResult** - Output della classificazione minacce
6. **BehaviorSignalExtractor** (interfaccia) - Contratto estrazione feature
7. **ThreatClassifier** (interfaccia) - Contratto classificazione
8. **AIHoneypotException** - Eccezione base
9. **SignalExtractionException** - Errori estrazione segnali
10. **ClassificationException** - Errori classificazione

### Modulo Collector (4 classi)

1. **SessionStore** - Tracciamento sessioni in memoria
2. **HttpServletSignalExtractor** - Estrae segnali dalle richieste servlet
3. **SignalCollectorFilter** - Filtro servlet che intercetta tutte le richieste
4. **IsolationForestAnomalyDetector** - Rilevamento anomalie non supervisionato utilizzando Weka

### Modulo Analyzer (5 classi)

1. **RuleBasedClassifier** - Rilevamento minacce euristico
2. **EnsembleClassifier** - Classificatore aggregato (Layer 3)
3. **ThreatSession** - Entità JPA per minacce persistite
4. **ThreatSessionRepository** - Repository Spring Data JPA
5. **ThreatLogService** - Servizio per logging minacce su DB

### Modulo Honeypot

1. **AIHoneypotApplication** - Classe main Spring Boot
2. **CanaryController** - Endpoint trappola
3. **TrafficSimulator** - Traffico realistico e simulazione attacchi
4. **DatabaseSeeder** - Popolamento dati iniziale
5. **application.properties** - Configurazione

---

## Endpoint Trappola Canary

I seguenti endpoint sono trappole che attivano la classificazione immediata della minaccia:

- /admin, /wp-admin
- /.env
- /config
- /api/internal
- /.git
- /backup

---

## Endpoint API

### API Dashboard

```
GET /api/dashboard/stats                  - Statistiche generali
GET /api/dashboard/threats/recent         - Minacce recenti
GET /api/dashboard/threats/last-hours     - Minacce in intervallo temporale
GET /api/dashboard/stats/by-client-type   - Conteggio per tipo client
GET /api/dashboard/stats/by-severity      - Conteggio per severità
GET /api/dashboard/stats/top-ips          - IP attaccanti principali
GET /api/dashboard/export/csv             - Esporta minacce in CSV
GET /api/dashboard/health                 - Health check
```

---

## Configurazione

Modifica honeypot/src/main/resources/application.properties:

```properties
# Database (H2 for dev, PostgreSQL for prod)
spring.datasource.url=jdbc:h2:mem:honeypotdb

# Soglia rilevamento minacce
honeypot.threat.confidence-threshold=0.5

# Abilita trappole canary
honeypot.canary.enabled=true
```

---

## Testing

Esegui i test su tutti i moduli:

```bash
mvn test
```

---

## Segnali di Rilevamento

Il sistema analizza questi segnali comportamentali:

- **Temporali**: Intervalli richieste, durata sessione, timing medio tra le richieste
- **Header**: Pattern User-Agent, header Accept mancanti, ordine header
- **Comportamentali**: Esecuzione JavaScript, gestione cookie (ove disponibile)
- **Navigazione**: Accesso diretto endpoint, referer mancante, profondità del path
- **Contenuto**: Attivazione trappole canary, complessità query, ratio caratteri sospetti
- **Network**: Analisi IP, tasso di richieste per minuto

---

## Classificazione Minacce

### Classificatore Basato su Regole

Utilizza euristiche per rilevare:
- Header HTTP mancanti (Accept, Accept-Language)
- Stringhe User-Agent simili a bot
- Firme agenti AI (GPT, Claude, LangChain)
- Pattern scanner sicurezza (Nikto, Burp)
- Accesso trappole canary
- Pattern richieste sospettosamente veloci

### Classificatore Isolation Forest (Rilevamento Anomalie)

Layer di machine learning non supervisionato che:
- Apprende i pattern di traffico normale
- Rileva deviazioni (anomalie) senza conoscenza previa dei tipi di attacco
- Utilizza 12 feature comportamentali incluse l'entropia del timing e la struttura dell'URI

### Classificatore Ensemble

Aggrega livelli di rilevamento multipli:
- Combina Layer 1 (Regole) e Layer 2 (ML)
- Risolve le discrepanze tra i classificatori
- Implementa mitigazioni teoriche per il teorema "No Free Lunch"

---

## Rilevamento Bot su X

Modulo specializzato per rilevare bot e agenti AI su X (Twitter) con analisi comportamentale avanzata.

### Caratteristiche

- **5 Analizzatori Indipendenti**: Profile, Network, Temporal, Text, Behavior
- **Predizioni Spiegabili**: Ogni classificazione include spiegazione dettagliata e segnali attivati
- **Pesi Configurabili**: Ogni analizzatore ha un peso regolabile nello score finale
- **Modalità Input Manuale**: Funziona senza API key di Twitter

### Suddivisione Analizzatori

| Analizzatore | Peso | Segnali Chiave |
|--------------|------|----------------|
| **ProfileAnalyzer** | 20% | Età account, pattern username, parole chiave AI in bio, immagine default |
| **NetworkAnalyzer** | 25% | Rapporto follower/following, spam di follow, anomalie engagement |
| **TemporalAnalyzer** | 25% | Frequenza post, intervalli regolari (CV), posting a raffica |
| **TextAnalyzer** | 30% | Pattern AI/LLM, ripetizione testo, client di automazione, vocabolario |
| **BehaviorAnalyzer** | 20% | Rapporto retweet, assenza risposte, client API, uniformità lingua |

### Livelli di Classificazione

| Range Score | Classificazione | Azione |
|-------------|-----------------|---------|
| < 0.3 | **PROBABILMENTE UMANO** | Consenti |
| 0.3 - 0.6 | **INCERTO** | Monitora |
| 0.6 - 0.8 | **PROBABILE BOT** | Revisiona |
| > 0.8 | **BOT CONFERMATO** | Blocca |

---

## Esempio Rilevamento

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

---

## Roadmap

I seguenti elementi sono pianificati per iterazioni future:

- **Espansione della copertura degli endpoint canary** oltre la lista core attuale
- **Segnali comportamentali più ricchi** (movimento mouse, fingerprinting JS avanzato)
- **Profondità di analisi della sicurezza estesa** con scoring multi-stadio

---

## Contribuire

I contributi sono benvenuti! Segui questi passaggi:

1. Fai fork del repository
2. Crea un branch per la feature
3. Apporta le tue modifiche
4. Aggiungi test
5. Invia una pull request

---

## Licenza

Questo progetto è rilasciato sotto licenza MIT.

---

## Autore

**Dania Ciampalini**

---

**Nota**: Questo è un sistema honeypot progettato per ricerca e monitoraggio della sicurezza. Distribuisci responsabilmente e assicura la conformità con le leggi e i regolamenti pertinenti.
