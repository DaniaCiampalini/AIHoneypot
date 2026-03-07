# AIHoneypot - AI Agent Detection System

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-green)
![Maven](https://img.shields.io/badge/Maven-Multi--Module-blue)

A sophisticated honeypot system for detecting and classifying AI agents, bots, and automated HTTP clients using behavioral fingerprinting and machine learning.

## 🎯 Features

- **Behavioral Fingerprinting**: Analyzes HTTP request patterns to distinguish humans from bots
- **Multi-Layer Classification**: 
  - Rule-based detection for known patterns
  - Isolation Forest for anomaly detection
  - Ensemble methods combining multiple classifiers
- **Canary Traps**: Decoy endpoints that no legitimate user should access
- **Real-time Threat Logging**: Persists threat sessions to database
- **REST API Dashboard**: Monitor and analyze detected threats
- **Session Tracking**: Correlates multiple requests from the same session

## 🏗️ Architecture

This is a Maven multi-module project with the following structure:

```
AIHoneypot/
├── core/              # Domain models, interfaces, exceptions
├── collector/         # Signal collection layer (servlet filters)
├── analyzer/          # Threat classification engine
├── dashboard/         # REST API and statistics
└── honeypot/          # Main Spring Boot application + canary traps
```

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

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- (Optional) PostgreSQL for production

### Build

```bash
mvn clean install
```

### Run

```bash
cd honeypot
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Access Dashboard

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
- **API Docs**: http://localhost:8080/api-docs

## 📊 Core Domain Classes

### Core Module (10 classes)

1. **ClientType** (enum) - HUMAN_BROWSER, AI_AGENT, BOT_SCRAPER, etc.
2. **Severity** (enum) - LOW, MEDIUM, HIGH, CRITICAL
3. **SignalType** (enum) - Types of behavioral signals
4. **RawRequestSignals** - Raw HTTP request data
5. **ClassificationResult** - Output of threat classification
6. **BehaviorSignalExtractor** (interface) - Feature extraction contract
7. **ThreatClassifier** (interface) - Classification contract
8. **AIHoneypotException** - Base exception
9. **SignalExtractionException** - Signal extraction errors
10. **ClassificationException** - Classification errors

### Collector Module (3 classes)

1. **SessionStore** - In-memory session tracking
2. **HttpServletSignalExtractor** - Extracts signals from servlet requests
3. **SignalCollectorFilter** - Servlet filter intercepting all requests

### Analyzer Module (4 classes)

1. **RuleBasedClassifier** - Heuristic threat detection
2. **ThreatSession** - JPA entity for persisted threats
3. **ThreatSessionRepository** - Spring Data JPA repository
4. **ThreatLogService** - Service for logging threats to DB

### Honeypot Module

1. **AIHoneypotApplication** - Spring Boot main class
2. **CanaryController** - Trap endpoints (/admin, /.env, etc.)
3. **application.properties** - Configuration

## 🕷️ Canary Trap Endpoints

The following endpoints are traps that trigger immediate threat classification:

- `/admin`, `/wp-admin` - Admin panel traps
- `/.env`, `/.env.local` - Environment file traps
- `/config`, `/configuration` - Config file traps
- `/api/internal` - Internal API trap
- `/.git`, `/.svn` - Version control traps
- `/backup`, `/*.bak` - Backup file traps
- `/*.sql`, `/dump.sql` - Database dump traps
- `/phpmyadmin` - PHPMyAdmin trap

## 📡 API Endpoints

### Dashboard API

```
GET /api/dashboard/stats                  - Overall statistics
GET /api/dashboard/threats/recent         - Recent threats
GET /api/dashboard/threats/last-hours     - Threats in time range
GET /api/dashboard/stats/by-client-type   - Count by client type
GET /api/dashboard/stats/by-severity      - Count by severity
GET /api/dashboard/stats/top-ips          - Top attacking IPs
GET /api/dashboard/health                 - Health check
```

## 🔧 Configuration

Edit `honeypot/src/main/resources/application.properties`:

```properties
# Database (H2 for dev, PostgreSQL for prod)
spring.datasource.url=jdbc:h2:mem:honeypotdb

# Threat detection threshold
honeypot.threat.confidence-threshold=0.5

# Enable canary traps
honeypot.canary.enabled=true
```

## 🧪 Testing

Run tests across all modules:

```bash
mvn test
```

## 📈 Detection Signals

The system analyzes these behavioral signals:

- **Timing**: Request intervals, session duration
- **Headers**: User-Agent patterns, missing Accept headers, header order
- **Behavioral**: Mouse movement, JavaScript execution, cookie handling
- **Navigation**: Direct endpoint access, missing referer
- **Content**: Form submission speed, canary trap triggers
- **Network**: IP reputation, geolocation

## 🛡️ Threat Classification

### Rule-Based Classifier

Uses heuristics to detect:
- Missing HTTP headers (Accept, Accept-Language)
- Bot-like User-Agent strings
- AI agent signatures (GPT, Claude, LangChain)
- Security scanner patterns (Nikto, Burp)
- Canary trap access
- Suspiciously fast request patterns

### Isolation Forest Classifier

Uses statistical anomaly detection to identify unusual behavioral patterns.

### Ensemble Classifier

Combines multiple classifiers for robust detection.

## 📝 Example Detection

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

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 👤 Author

**Dania Ciampalini**

---

⚠️ **Note**: This is a honeypot system designed for research and security monitoring. Deploy responsibly and ensure compliance with relevant laws and regulations.

