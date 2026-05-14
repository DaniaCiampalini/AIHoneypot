
# AIHoneypot - AI Agent Detection System

A sophisticated honeypot system for detecting and classifying AI agents, bots, and automated HTTP clients using behavioral fingerprinting and rule-based heuristics.

---

## Documentation in Other Languages

- [English](README.md) (Current)
- [Italiano](docs/README_IT.md)
- [Español](docs/README_ES.md)
- [Deutsch](docs/README_DE.md)

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

## Threat Classification

- **Layer 1: Rule-Based Classifier**: Heuristic detection based on headers, timing, and canary access (Decidable by construction).
- **Layer 2: Isolation Forest Anomaly Detection**: Unsupervised ML for detecting unknown attack patterns and distribution shifts (Thesis study case).
- **Layer 3: Ensemble Detection**: Integration of rule-based and ML approaches to overcome individual theoretical limits (No Free Lunch theorem).

---

## Thesis: Theoretical limits of ML systems for security

This project is structured as a study case for a thesis on automated detection limits.
- **Test Scenarios**:
  1. **Known Patterns**: Effectiveness of the Rule-Based Classifier.
  2. **Unknown Anomalies**: Effectiveness of the Isolation Forest on zero-day attacks.
  3. **Borderline Traffic**: Analysis of false positives/negatives and PAC-learning limits.
- **Weka Integration**: Use of the isolationForest package for statistical traffic analysis.

- **Canary Traps**: Decoy endpoints that no legitimate user should access
  - /admin, /wp-admin
  - /.env
  - /config
  - /api/internal
  - /.git
  - /backup
  - Automatic CRITICAL severity on access

- **Real-time Threat Logging**: Persists threat sessions to H2 database

- **REST API Dashboard**: Monitor and analyze detected threats via HTTP endpoints
  - CSV Export: Export data for experimental analysis and thesis evaluation
  - Stats Comparative: Statistics on discrepancies between Rule-based and ML layers

- **Session Tracking**: Correlates multiple requests from the same session
  - Advanced Feature Engineering: Tracking request rates, average timing, and URI structural patterns

---

## Architecture

This is a Maven multi-module project with the following structure:

```
AIHoneypot/
├── docs/              # Translated README files
├── scripts/           # Startup shell scripts
├── core/              # Domain models, interfaces, exceptions
├── collector/         # Signal collection layer (servlet filters and IF detector)
├── analyzer/          # Threat classification engine (Rules and Ensemble)
├── dashboard/         # REST API and statistics
└── honeypot/          # Main Spring Boot application + canary traps + traffic simulator
```

### Technology Stack

- **Backend**: Spring Boot 3.2.2, Java 17
- **Database**: H2 (in-memory)
- **ML/AI**: Weka Isolation Forest, Rule-based heuristics, Ensemble methods
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
2. **HttpServletSignalExtractor** - Extracts signals from servlet requests  
3. **SignalCollectorFilter** - Servlet filter intercepting all requests  
4. **IsolationForestAnomalyDetector** - Unsupervised anomaly detection using Weka

### Analyzer Module

1. **RuleBasedClassifier** - Heuristic threat detection  
2. **EnsembleClassifier** - Aggregate classifier (Layer 3)
3. **ThreatSession** - JPA entity for persisted threats  
4. **ThreatLogService** - Service for logging threats to DB  

### Honeypot Module

1. **AIHoneypotApplication** - Spring Boot main class  
2. **CanaryController** - Trap endpoints  
3. **TrafficSimulator** - Realistic traffic and attack simulation
4. **DatabaseSeeder** - Initial data seeding

---

## Canary Trap Endpoints

The following endpoints are traps that trigger immediate threat classification:

- /admin, /wp-admin  
- /.env  
- /config  
- /api/internal  
- /.git  
- /backup  

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

The system analyzes these behavioral signals:

- **Timing**: Request intervals, session duration, average timing between requests
- **Headers**: User-Agent patterns, missing Accept headers, header complexity  
- **Navigation**: Direct endpoint access, missing referer, path depth
- **Content**: Canary trap triggers, query complexity, suspicious characters ratio
- **Network**: IP analysis, request rate

---

## Threat Classification

### Rule-Based Classifier

Uses heuristics to detect:
- Missing HTTP headers (Accept, Accept-Language)
- Bot-like User-Agent strings
- AI agent signatures (GPT, Claude, LangChain)
- Security scanner patterns (Nikto, Burp)
- Canary trap access
- Suspiciously fast request patterns

### Isolation Forest Classifier (Anomaly Detection)

Unsupervised machine learning layer that:
- Learns normal traffic patterns
- Detects deviations (anomalies) without prior knowledge of attack types
- Uses 12 behavioral features including timing entropy and URI structure

### Ensemble Classifier

Aggregates multiple detection layers:
- Combines Layer 1 (Rules) and Layer 2 (ML)
- Resolves discrepancies between classifiers
- Implements theoretical mitigations for No Free Lunch theorem

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

- **Frontend Dashboard**: Re-implementing the web-based or desktop monitoring interface
- **Expanded canary endpoint coverage** beyond the current core list  
- **Advanced behavioral signals** such as JavaScript-based fingerprinting
- **Explainability module** for ML-based detections

---

## License

This project is licensed under the MIT License.

---

## Author

**Dania Ciampalini**

---

**Note**: This is a honeypot system designed for research and security monitoring. Deploy responsibly and ensure compliance with relevant laws and regulations.