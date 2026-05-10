```markdown
# AIHoneypot - AI Agent Detection System

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-green)
![Maven](https://img.shields.io/badge/Maven-Multi--Module-blue)
![License](https://img.shields.io/badge/License-MIT-blue)

A sophisticated honeypot system for detecting and classifying AI agents, bots, and automated HTTP clients using behavioral fingerprinting and rule-based heuristics.

---

## 🌍 Documentation in Other Languages

- 🇬🇧 [English](README.md) (Current)
- 🇮🇹 [Italiano](docs/README_IT.md)
- 🇫🇷 [Français](docs/README_FR.md)
- 🇪🇸 [Español](docs/README_ES.md)
- 🇩🇪 [Deutsch](docs/README_DE.md)
- 🇨🇳 [简体中文](docs/README_ZH.md)
- 🇯🇵 [日本語](docs/README_JA.md)
- 🇷🇺 [Русский](docs/README_RU.md)

---

## 🎯 Features

- **🎨 Interactive GUI Dashboard**: Beautiful JavaFX desktop application with real-time monitoring
  - Live threat statistics with auto-refresh
  - Interactive charts (Pie & Bar charts) for data visualization
  - Recent threats table with detailed information
  - Top attacking IPs analysis
  - iOS-inspired modern theme with smooth animations
  
- **🚗 Traffic Simulator**: Generates realistic honeypot traffic for testing
  - Automatic realistic traffic generation every 5 seconds
  - Attack simulations every 30 seconds
  - Burst traffic waves every 2 minutes for stress testing
  - Multiple client types (humans, bots, AI agents, scanners)

- **🌱 Database Seeding**: Pre-populates database with historical data
  - ~270 initial threat sessions
  - 7 days of historical patterns
  - Realistic attack distributions by severity (including SQL injection and bot scan spikes)

- **🔍 Behavioral Fingerprinting**: Analyzes HTTP request patterns to distinguish humans from bots

- **🧠 Threat Classification (Rule-Based)**:
  - Heuristic detection based on headers, timing, and canary access
  - AI agent, bot scraper, and security scanner identification

- **🐦 X (Twitter) Bot Detection**: Specialized module for detecting bots and AI agents on social media
  - Profile analysis (age, username patterns, bio detection)
  - Network analysis (follower/following ratios)
  - Temporal patterns (posting frequency)
  - AI-generated text detection
  - Explainable scoring and recommendations

- **🕸️ Canary Traps**: Decoy endpoints that no legitimate user should access
  - `/admin`, `/wp-admin`
  - `/.env`
  - `/config`
  - `/api/internal`
  - `/.git`
  - `/backup`
  - Automatic CRITICAL severity on access

- **💾 Real-time Threat Logging**: Persists threat sessions to H2/PostgreSQL database

- **📊 REST API Dashboard**: Monitor and analyze detected threats via HTTP endpoints

- **🔄 Session Tracking**: Correlates multiple requests from the same session

- **🔒 Website Security Analysis (GUI)**:
  - SSL/HTTPS checks
  - Security header review
  - Heuristic vulnerability and risk scoring

---

## 🏗️ Architecture

This is a Maven multi-module project with the following structure:

```
AIHoneypot/
├── docs/              # Translated README files
├── scripts/           # Startup shell scripts
├── core/              # Domain models, interfaces, exceptions
├── collector/         # Signal collection layer (servlet filters)
├── analyzer/          # Threat classification engine
├── dashboard/         # REST API and statistics
├── gui/               # JavaFX Desktop Dashboard
├── x-detector         # X (Twitter) bot detection module
└── honeypot/          # Main Spring Boot application + canary traps + traffic simulator
```

### Technology Stack

- **Backend**: Spring Boot 3.2.2, Java 17
- **Frontend**: JavaFX 21 (Desktop GUI)
- **Database**: H2 (in-memory) / PostgreSQL (production)
- **ML/AI**: Rule-based classification (ML planned; see Roadmap)
- **Charts**: JavaFX Charts API
- **Build**: Maven Multi-Module

### Module Dependencies

```
honeypot (main)
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

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- (Optional) PostgreSQL for production

### Option 1: Complete Startup (Backend + GUI) - RECOMMENDED

Start everything with a single command:

```bash
./scripts/start-complete.sh
```

This script will:
1. ✅ Check if backend is running  
2. ✅ Build project if needed  
3. ✅ Start backend in background  
4. ✅ Wait for backend to be ready  
5. ✅ Launch GUI Dashboard  

**Note**: The backend runs in background. Logs are in `/tmp/aihoneypot-backend.log`

### Option 2: Manual Startup

#### Start Backend

```bash
cd honeypot
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

#### Start GUI (in another terminal)

```bash
cd gui
mvn javafx:run
```

Or use the GUI-only script:

```bash
./scripts/start-gui-only.sh
```

### Option 3: GUI Only (Backend Already Running)

If the backend is already running:

```bash
./scripts/start-gui-only.sh
```

This checks if backend is up and starts the GUI.

### Stop Backend

```bash
# Find and kill backend process
pkill -f 'spring-boot:run'

# Or find PID and kill
lsof -ti:8080 | xargs kill -9
```

### Access Points

- **GUI Dashboard**: Automatically opens when running `./scripts/start-complete.sh` or `./scripts/start-gui-only.sh`
- **API Swagger**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
- **API Docs**: http://localhost:8080/api-docs
- **Health Check**: http://localhost:8080/actuator/health

---

## 📊 Core Domain Classes

### Core Module (10 classes)

1. **ClientType** (enum) - HUMAN_BROWSER, AI_AGENT, BOT_SCRAPER, SEARCH_ENGINE, SECURITY_SCANNER, UNKNOWN  
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
2. **CanaryController** - Trap endpoints  
3. **application.properties** - Configuration  

---

## 🕷️ Canary Trap Endpoints

The following endpoints are traps that trigger immediate threat classification:

- `/admin`, `/wp-admin`  
- `/.env`  
- `/config`  
- `/api/internal`  
- `/.git`  
- `/backup`  

---

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

---

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

---

## 🧪 Testing

Run tests across all modules:

```bash
mvn test
```

---

## 📈 Detection Signals

The system analyzes these behavioral signals:

- **Timing**: Request intervals, session duration  
- **Headers**: User-Agent patterns, missing Accept headers, header order  
- **Behavioral**: JavaScript detection, cookie handling (as available)  
- **Navigation**: Direct endpoint access, missing referer  
- **Content**: Canary trap triggers  
- **Network**: IP analysis (as available)

---

## 🛡️ Threat Classification

### Rule-Based Classifier

Uses heuristics to detect:
- Missing HTTP headers (Accept, Accept-Language)
- Bot-like User-Agent strings
- AI agent signatures (GPT, Claude, LangChain)
- Security scanner patterns (Nikto, Burp)
- Canary trap access
- Suspiciously fast request patterns

---

## 🤖 X Bot Detector

Specialized module for detecting bots and AI agents on X (Twitter) with advanced behavioral analysis.

### Features

- **5 Independent Analyzers**: Profile, Network, Temporal, Text, Behavior  
- **Explainable Predictions**: Each classification includes detailed explanation and triggered signals  
- **Configurable Weights**: Each analyzer has adjustable weight in final score  
- **Manual Input Mode**: Works without Twitter API key  

### Analyzers Breakdown

| Analyzer | Weight | Key Signals |
|----------|--------|-------------|
| **ProfileAnalyzer** | 20% | Account age, username patterns, AI keywords in bio, default image |
| **NetworkAnalyzer** | 25% | Follower/following ratio, follow spam, engagement anomalies |
| **TemporalAnalyzer** | 25% | Posting frequency, regular intervals (CV), burst posting |
| **TextAnalyzer** | 30% | AI/LLM patterns, text repetition, automation clients, vocabulary |
| **BehaviorAnalyzer** | 20% | Retweet ratio, no replies, API clients, language uniformity |

### Classification Levels

| Score Range | Classification | Action |
|-------------|---------------|---------|
| < 0.3 | **LIKELY_HUMAN** | ✅ Allow |
| 0.3 - 0.6 | **UNCERTAIN** | ⚠️ Monitor |
| 0.6 - 0.8 | **LIKELY_BOT** | 🔍 Review |
| > 0.8 | **CONFIRMED_BOT** | 🚫 Block |

### Example X Detector Request

```json
{
  "username": "suspicious_bot",
  "displayName": "AI Helper",
  "bio": "As an AI, I am here to help you!",
  "verified": false,
  "accountCreatedDate": "2024-03-01T00:00:00Z",
  "followersCount": 50,
  "followingCount": 5000,
  "tweetCount": 10000,
  "defaultProfileImage": true,
  "recentTweetTexts": [
    "As an AI assistant, I can help with your needs.",
    "Here are 5 ways to improve productivity...",
    "It is important to note that consistency is key."
  ],
  "tweetSources": ["Buffer", "Buffer", "Buffer"]
}
```

### Example X Detector Response

```json
{
  "username": "suspicious_bot",
  "score": 0.87,
  "classification": "CONFIRMED_BOT",
  "confidence": 0.92,
  "explanation": "CONFIRMED BOT: Account @suspicious_bot has a bot probability of 87.0%. Red flags: TextAnalyzer detected high bot probability (0.85), ProfileAnalyzer detected high bot probability (0.72), NetworkAnalyzer detected high bot probability (0.68).",
  "categoryScores": {
    "ProfileAnalyzer": 0.72,
    "NetworkAnalyzer": 0.68,
    "TemporalAnalyzer": 0.55,
    "TextAnalyzer": 0.85,
    "BehaviorAnalyzer": 0.60
  },
  "redFlags": [
    "TextAnalyzer detected high bot probability (0.85)",
    "ProfileAnalyzer detected high bot probability (0.72)",
    "NetworkAnalyzer detected high bot probability (0.68)"
  ],
  "recommendation": "BLOCK - High confidence bot detection. Recommend blocking or flagging."
}
```

### Detected AI/LLM Patterns

The Text Analyzer detects common AI-generated content patterns:

- "As an AI..." / "I'm an AI..."
- "I don't have personal..." / "I cannot..."
- "My programming..." / "My training data..."
- "I'm here to help..." / "Happy to assist..."
- "Here are X ways/tips/reasons..."
- "It's important to note..." / "It's worth noting..."
- "In conclusion..." / "To summarize..."

For more details, see [x-detector/README.md](x-detector/README.md)

---

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

---

## 🧭 Roadmap (Planned Enhancements)

The following items are not yet implemented but are planned for future iterations:

- **Isolation Forest classifier** for statistical anomaly detection  
- **Ensemble classifier** combining multiple signals/classifiers  
- **Expanded canary endpoint coverage** beyond the current core list  
- **Richer behavioral signals** (mouse movement, advanced JS fingerprinting)  
- **Extended security analysis depth** with multi-stage scoring  

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository  
2. Create a feature branch  
3. Make your changes  
4. Add tests  
5. Submit a pull request  

---

## 📄 License

This project is licensed under the MIT License.

---

## 👤 Author

**Dania Ciampalini**

---

⚠️ **Note**: This is a honeypot system designed for research and security monitoring. Deploy responsibly and ensure compliance with relevant laws and regulations.
```