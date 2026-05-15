
# AIHoneypot - AI Agent Detection System

A sophisticated honeypot system for detecting and classifying AI agents, bots, and automated HTTP clients using behavioral fingerprinting and machine learning.

---

## Documentation in Other Languages

- [English](README.md) (Current)
- [Italiano](docs/README_IT.md)
- [Español](docs/README_ES.md)
- [Deutsch](docs/README_DE.md)

---

## What is a Honeypot?

A honeypot is a security tool designed to attract and detect attackers and unauthorized access attempts. Think of it like a decoy system that looks valuable to attackers but is actually isolated and monitored. When someone tries to break in or access forbidden areas, the honeypot records exactly what they did. This helps security professionals understand attack patterns and improve their defenses.

## What is Behavioral Fingerprinting?

Behavioral fingerprinting is the process of identifying who or what is making requests to your system by analyzing *how* they interact with it, rather than just *what* they request. For example:
- Humans browse inconsistently and slowly; bots make rapid, repetitive requests
- Real browsers send specific headers; scrapers often omit them
- Humans visit pages in a logical flow; attackers probe for known vulnerabilities in a systematic pattern

By collecting these behavioral signals, AIHoneypot can distinguish between legitimate users, AI agents, bots, and security scanners.

---

## Features

- **Traffic Simulator**: Generates realistic honeypot traffic for testing
  - Automatic realistic traffic generation every 5 seconds
  - Attack simulations every 30 seconds
  - Burst traffic waves every 2 minutes for stress testing
  - Multiple client types (humans, bots, AI agents, scanners)

- **Database Seeding**: Pre-populates database with historical data
  - ~270 initial threat sessions
  - 7 days of historical patterns
  - Realistic attack distributions by severity (including SQL injection and bot scan spikes)

- **Behavioral Fingerprinting**: Analyzes HTTP request patterns to distinguish humans from bots

- **Real-time Threat Logging**: Persists threat sessions to H2/PostgreSQL database

- **REST API Dashboard**: Monitor and analyze detected threats via HTTP endpoints
  - Comparative statistics between rule-based and machine learning classifications
  - CSV export for offline analysis
  - Threat breakdown by client type, severity, and time range

- **Session Tracking**: Correlates multiple requests from the same session
  - Feature engineering: request rate, inter-request timing, URI structural patterns, header completeness score

---

## Multi-Layer Threat Classification

AIHoneypot uses a two-layer classification architecture to detect threats with high accuracy:

### Layer 1: Rule-Based Classifier

This layer uses predefined rules (heuristics) to detect known threats. It's like having an expert security analyst who knows all the telltale signs of an attack.

**How it works:**
- Detects known bot signatures (User-Agent patterns)
- Identifies missing HTTP headers (real browsers always include certain headers)
- Recognizes AI agent patterns (e.g., GPT, Claude, LangChain)
- Spots security scanner tools (e.g., Nikto, Burp)
- Triggers on suspicious timing patterns (requests too fast to be human)
- Detects access to canary traps (decoy endpoints)

**Advantages:** Fast, interpretable, reliable for known threats
**Limitations:** Cannot detect completely new attack patterns

### Layer 2: Isolation Forest (Machine Learning Anomaly Detection)

This layer uses unsupervised machine learning to detect *unusual* behavior without requiring predefined rules. Think of it as learning what "normal" traffic looks like, then flagging anything that deviates significantly.

**How it works:**
- Analyzes 12 behavioral features extracted from each request
- Learns the baseline patterns from normal traffic
- Identifies sessions that deviate significantly from the baseline
- Can detect novel attacks not covered by Layer 1 rules
- Operates without needing examples of every possible attack type

**Advantages:** Detects unknown attack patterns, adapts to new threats
**Limitations:** Less interpretable than rules (harder to explain *why* something was flagged)

### Layer 3: Ensemble Integration

Combines both layers to overcome individual limitations. If one classifier might miss an attack, the other likely won't.

---

## Canary Traps

Canary traps are decoy endpoints that no legitimate user should ever access. They serve as tripwires that immediately flag suspicious activity:

- `/admin`, `/wp-admin` — Admin panels
- `/.env` — Configuration files
- `/config` — Settings
- `/api/internal` — Internal APIs
- `/.git` — Version control
- `/backup` — Backup files

Accessing any canary trap automatically results in a CRITICAL severity threat classification, because legitimate users have no reason to look for these.

---

## Architecture

This is a Maven multi-module project with the following structure:

```
AIHoneypot/
├── docs/              # Translated README files
├── scripts/           # Startup shell scripts
├── core/              # Domain models, interfaces, exceptions
├── collector/         # Signal collection layer (servlet filters and ML detector)
├── analyzer/          # Threat classification engine (Rules and Ensemble)
├── dashboard/         # REST API and statistics
└── honeypot/          # Main Spring Boot application + canary traps + traffic simulator
```

### Technology Stack

- **Backend**: Spring Boot 3.2.2, Java 17
- **Database**: H2 (in-memory) or PostgreSQL
- **Machine Learning**: Weka Isolation Forest
- **Build**: Maven Multi-Module

### Module Dependencies

```
honeypot (main)
├── dashboard
│   └── analyzer
│       └── core
├── collector
│   └── core
└── analyzer
    └── core
```

---

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+

### Startup

#### Start Backend

```bash
cd honeypot
mvn spring-boot:run
```

The application will start on http://localhost:8080

### Access Points

- **API Swagger**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
- **API Docs**: http://localhost:8080/api-docs
- **Health Check**: http://localhost:8080/actuator/health

---

## Core Domain Classes

### Core Module

1. **ClientType** (enum) - HUMAN_BROWSER, AI_AGENT, BOT_SCRAPER, SEARCH_ENGINE, SECURITY_SCANNER, UNKNOWN
2. **Severity** (enum) - LOW, MEDIUM, HIGH, CRITICAL
3. **RawRequestSignals** - Raw HTTP request data
4. **ClassificationResult** - Output of threat classification
5. **ThreatClassifier** (interface) - Classification contract

### Collector Module

1. **SessionStore** - In-memory session tracking
2. **HttpServletSignalExtractor** - Extracts behavioral signals from servlet requests
3. **SignalCollectorFilter** - Servlet filter intercepting all requests
4. **IsolationForestAnomalyDetector** - Unsupervised anomaly detection using Weka

### Analyzer Module

1. **RuleBasedClassifier** - Heuristic threat detection
2. **EnsembleClassifier** - Aggregate classifier combining multiple detection methods
3. **ThreatSession** - JPA entity for persisted threats
4. **ThreatLogService** - Service for logging threats to database

### Honeypot Module

1. **AIHoneypotApplication** - Spring Boot main class
2. **CanaryController** - Trap endpoint handlers
3. **TrafficSimulator** - Realistic traffic and attack simulation
4. **DatabaseSeeder** - Initial data seeding

---

## API Endpoints

### Dashboard API

```
GET /api/dashboard/stats                  - Overall statistics
GET /api/dashboard/threats/recent         - Recent threats
GET /api/dashboard/threats/last-hours     - Threats in time range
GET /api/dashboard/stats/by-client-type   - Count by client type
GET /api/dashboard/stats/by-severity      - Count by severity
GET /api/dashboard/stats/top-ips          - Top attacking IPs
GET /api/dashboard/export/csv             - Export threats to CSV
GET /api/dashboard/health                 - Health check
```

---

## Configuration

Edit honeypot/src/main/resources/application.properties:

```properties
# Database (H2)
spring.datasource.url=jdbc:h2:mem:honeypotdb

# Threat detection threshold
honeypot.threat.confidence-threshold=0.5

# Enable canary traps
honeypot.canary.enabled=true
```

---

## Testing

Run tests across all modules:

```bash
mvn test
```

---

## Detection Signals

The system analyzes these behavioral signals to identify threats:

- **Timing**: Request intervals, session duration, average timing between requests
- **Headers**: User-Agent patterns, missing Accept headers, header completeness score
- **Navigation**: Direct endpoint access, missing referer, path depth
- **Content**: Canary trap triggers, query complexity, suspicious characters ratio
- **Network**: IP analysis, request rate

---

## Example Detection

```json
{
  "sessionId": "ABC123",
  "timestamp": "2024-03-07T10:30:00Z",
  "clientType": "AI_AGENT",
  "confidence": 0.85,
  "severity": "HIGH",
  "isThreat": true,
  "explanation": "AI agent User-Agent detected. Canary trap triggered. Missing Accept header.",
  "triggeredFeatures": {
    "canary_trap": true,
    "ai_agent_user_agent": true,
    "missing_accept_header": true
  }
}
```

---

## Roadmap

The following items are planned for future iterations:

- **Frontend Dashboard**: Web-based monitoring interface
- **Expanded canary endpoint coverage** beyond the current core list
- **Advanced behavioral signals** such as JavaScript-based fingerprinting
- **Explainability module** for machine learning detections
- **Distributed deployment** support for large-scale honeypot networks

---

## License

This project is licensed under the MIT License.

---

## Author

**Dania Ciampalini**

---

**Note**: This is a honeypot system designed for security research and monitoring. Deploy responsibly and ensure compliance with relevant laws and regulations in your jurisdiction.
