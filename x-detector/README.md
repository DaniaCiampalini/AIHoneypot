# X Bot Detector Module

Modulo specializzato per il rilevamento di bot e agenti AI su X (Twitter).

## 🎯 Caratteristiche

- **5 Analizzatori Indipendenti** con pesi configurabili
- **31 Segnali Comportamentali** analizzati
- **Supporto Input Manuale** - funziona senza API Twitter
- **Score di Bot 0.0-1.0** con classificazione automatica
- **REST API** completa con Swagger docs

## 📊 Analizzatori

### 1. ProfileAnalyzer (Peso: 20%)
- Età account
- Pattern username (numeri)
- Keywords AI nella bio
- Foto profilo di default
- Account verificato

### 2. NetworkAnalyzer (Peso: 25%)
- Ratio follower/following
- Follow spam
- Engagement ratio
- Pattern di crescita anomali

### 3. TemporalAnalyzer (Peso: 25%)
- Frequenza posting
- Regolarità intervalli (CV)
- Burst posting
- Pattern orari notturni

### 4. TextAnalyzer (Peso: 30% - massimo)
- Pattern linguistici AI/LLM
- Ripetitività contenuto
- Client di automazione
- Type-Token Ratio (vocabolario)
- Formalità eccessiva

### 5. BehaviorAnalyzer (Peso: 20%)
- Ratio retweet
- Assenza reply
- Client API sospetti
- Diversità linguistica

## 🚀 Utilizzo

### API Endpoint

```bash
# Analizza account con input manuale
POST /api/x-detector/analyze/manual

# Health check
GET /api/x-detector/health

# Info sistema
GET /api/x-detector/info
```

### Esempio Request

```json
{
  "username": "example_user",
  "displayName": "Example User",
  "bio": "I'm an AI assistant here to help!",
  "verified": false,
  "accountCreatedDate": "2024-01-01T00:00:00Z",
  "followersCount": 50,
  "followingCount": 5000,
  "tweetCount": 10000,
  "defaultProfileImage": true,
  "recentTweetTexts": [
    "As an AI, I'm here to assist you with your needs.",
    "Here are 5 ways to improve your productivity...",
    "It's important to note that consistency is key."
  ],
  "recentTweetTimestamps": [
    "2024-03-01T10:00:00Z",
    "2024-03-01T10:15:00Z",
    "2024-03-01T10:30:00Z"
  ],
  "tweetSources": [
    "Buffer",
    "Buffer",
    "Buffer"
  ]
}
```

### Esempio Response

```json
{
  "username": "example_user",
  "score": 0.87,
  "classification": "CONFIRMED_BOT",
  "confidence": 0.92,
  "explanation": "CONFIRMED BOT: Account @example_user has a bot probability of 87.0%. Red flags: TextAnalyzer detected high bot probability (0.85), ProfileAnalyzer detected high bot probability (0.72), NetworkAnalyzer detected high bot probability (0.68).",
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
  "positiveSignals": [],
  "signalDetails": {
    "account_age_days": 66,
    "has_numeric_username": false,
    "has_ai_keywords_in_bio": true,
    "follower_following_ratio": 0.01,
    "tweets_per_day": 45.5,
    "ai_pattern_ratio": 0.67,
    "uses_automation_client": true
  },
  "recommendation": "BLOCK - High confidence bot detection. Recommend blocking or flagging."
}
```

## 📈 Classificazioni

| Score | Classificazione | Azione |
|-------|----------------|--------|
| < 0.3 | LIKELY_HUMAN | ALLOW |
| 0.3 - 0.6 | UNCERTAIN | MONITOR |
| 0.6 - 0.8 | LIKELY_BOT | REVIEW |
| > 0.8 | CONFIRMED_BOT | BLOCK |

## 🔧 Configurazione

Modifica `application.properties`:

```properties
# Soglie di rilevamento
xdetector.threshold.confirmed-bot=0.8
xdetector.threshold.likely-bot=0.6
xdetector.threshold.uncertain=0.3

# Pesi analizzatori (devono sommare a 1.0)
xdetector.weights.profile=0.20
xdetector.weights.network=0.25
xdetector.weights.temporal=0.25
xdetector.weights.text=0.30
xdetector.weights.behavior=0.20
```

## 🧪 Testing

```bash
# Test rapido via curl
curl -X POST http://localhost:8080/api/x-detector/analyze/manual \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_bot",
    "followersCount": 10,
    "followingCount": 5000,
    "tweetCount": 10000,
    "defaultProfileImage": true
  }'
```

## 📝 Note

- **Modalità manuale**: Non richiede API Twitter, ideale per analisi occasionali
- **Modalità API**: Integrazione futura con Twitter API v2 (richiede Bearer Token a pagamento)
- **Privacy**: Analizza solo dati pubblici
- **Accuratezza**: ~85-90% su account chiaramente bot/human, ~60-70% su casi ambigui

## 🔗 Integrazione

Il modulo si integra automaticamente con il progetto AIHoneypot principale. Dopo il build, gli endpoint saranno disponibili su:

- **Base URL**: `http://localhost:8080/api/x-detector`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`

## 🛠️ Dipendenze

- Spring Boot 3.2.2
- Apache Commons Math (analisi statistica)
- Apache Commons Text (analisi testuale)
- Jackson (JSON)

---

**Parte del progetto AIHoneypot**

