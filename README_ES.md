# AIHoneypot - Sistema de Detección de Agentes IA

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-green)
![Maven](https://img.shields.io/badge/Maven-Multi--Module-blue)
![License](https://img.shields.io/badge/License-MIT-blue)

Un sofisticado sistema honeypot para detectar y clasificar agentes IA, bots y clientes HTTP automatizados utilizando huellas dactilares comportamentales y aprendizaje automático.

---

## 🌍 Documentación en Otros Idiomas

- 🇬🇧 [English](README.md)
- 🇮🇹 [Italiano](README_IT.md)
- 🇫🇷 [Français](README_FR.md)
- 🇪🇸 [Español](README_ES.md) (Actual)
- 🇩🇪 [Deutsch](README_DE.md)
- 🇨🇳 [简体中文](README_ZH.md)
- 🇯🇵 [日本語](README_JA.md)
- 🇷🇺 [Русский](README_RU.md)

---

## 🎯 Características

- **Huella Digital Comportamental**: Analiza patrones de solicitudes HTTP para distinguir humanos de bots
- **Clasificación Multi-Capa**: 
  - Detección basada en reglas para patrones conocidos
  - Bosque de Aislamiento para detección de anomalías
  - Métodos de conjunto que combinan múltiples clasificadores
- **Detección de Bots en X (Twitter)**: Módulo especializado para detectar bots y agentes IA en redes sociales
- **Trampas Canarias**: Endpoints señuelo que ningún usuario legítimo debería acceder
- **Registro de Amenazas en Tiempo Real**: Persiste sesiones de amenazas en la base de datos
- **Panel de Control API REST**: Monitorear y analizar amenazas detectadas
- **Seguimiento de Sesiones**: Correlaciona múltiples solicitudes de la misma sesión

## 🏗️ Arquitectura

Este es un proyecto Maven multi-módulo con la siguiente estructura:

```
AIHoneypot/
├── core/              # Modelos de dominio, interfaces, excepciones
├── collector/         # Capa de recolección de señales (filtros servlet)
├── analyzer/          # Motor de clasificación de amenazas
├── dashboard/         # API REST y estadísticas
├── x-detector/        # Módulo de detección de bots X (Twitter)
└── honeypot/          # Aplicación Spring Boot principal + trampas canarias
```

### Dependencias de Módulos

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

## 🚀 Inicio Rápido

### Requisitos Previos

- Java 17+
- Maven 3.8+
- (Opcional) PostgreSQL para producción

### Construcción

```bash
mvn clean install
```

### Ejecución

```bash
cd honeypot
mvn spring-boot:run
```

La aplicación se iniciará en `http://localhost:8080`

### Acceso al Panel de Control

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Consola H2**: http://localhost:8080/h2-console
- **Documentación API**: http://localhost:8080/api-docs

## 📊 Clases de Dominio Core

### Módulo Core (10 clases)

1. **ClientType** (enum) - HUMAN_BROWSER, AI_AGENT, BOT_SCRAPER, etc.
2. **Severity** (enum) - LOW, MEDIUM, HIGH, CRITICAL
3. **SignalType** (enum) - Tipos de señales comportamentales
4. **RawRequestSignals** - Datos de solicitud HTTP sin procesar
5. **ClassificationResult** - Resultado de la clasificación de amenazas
6. **BehaviorSignalExtractor** (interfaz) - Contrato de extracción de características
7. **ThreatClassifier** (interfaz) - Contrato de clasificación
8. **AIHoneypotException** - Excepción base
9. **SignalExtractionException** - Errores de extracción de señales
10. **ClassificationException** - Errores de clasificación

### Módulo Collector (3 clases)

1. **SessionStore** - Seguimiento de sesiones en memoria
2. **HttpServletSignalExtractor** - Extrae señales de solicitudes servlet
3. **SignalCollectorFilter** - Filtro servlet que intercepta todas las solicitudes

### Módulo Analyzer (4 clases)

1. **RuleBasedClassifier** - Detección heurística de amenazas
2. **ThreatSession** - Entidad JPA para amenazas persistidas
3. **ThreatSessionRepository** - Repositorio Spring Data JPA
4. **ThreatLogService** - Servicio para registro de amenazas en BD

### Módulo Honeypot

1. **AIHoneypotApplication** - Clase principal Spring Boot
2. **CanaryController** - Endpoints trampa (/admin, /.env, etc.)
3. **application.properties** - Configuración

## 🕷️ Endpoints Trampa Canaria

Los siguientes endpoints son trampas que activan clasificación inmediata de amenazas:

- `/admin`, `/wp-admin` - Trampas panel de administración
- `/.env`, `/.env.local` - Trampas archivos de entorno
- `/config`, `/configuration` - Trampas archivos de configuración
- `/api/internal` - Trampa API interna
- `/.git`, `/.svn` - Trampas control de versiones
- `/backup`, `/*.bak` - Trampas archivos de respaldo
- `/*.sql`, `/dump.sql` - Trampas volcado de base de datos
- `/phpmyadmin` - Trampa PHPMyAdmin

## 📡 Endpoints API

### API Panel de Control

```
GET /api/dashboard/stats                  - Estadísticas generales
GET /api/dashboard/threats/recent         - Amenazas recientes
GET /api/dashboard/threats/last-hours     - Amenazas en rango de tiempo
GET /api/dashboard/stats/by-client-type   - Conteo por tipo de cliente
GET /api/dashboard/stats/by-severity      - Conteo por severidad
GET /api/dashboard/stats/top-ips          - IPs atacantes principales
GET /api/dashboard/health                 - Control de salud
```

### API X Detector

```
POST /api/x-detector/analyze/manual       - Analizar cuenta X (entrada manual)
GET  /api/x-detector/health               - Control de salud
GET  /api/x-detector/info                 - Información del sistema
```

## 🔧 Configuración

Editar `honeypot/src/main/resources/application.properties`:

```properties
# Base de datos (H2 para dev, PostgreSQL para prod)
spring.datasource.url=jdbc:h2:mem:honeypotdb

# Umbral de detección de amenazas
honeypot.threat.confidence-threshold=0.5

# Habilitar trampas canarias
honeypot.canary.enabled=true
```

## 🧪 Pruebas

Ejecutar pruebas en todos los módulos:

```bash
mvn test
```

## 📈 Señales de Detección

El sistema analiza estas señales comportamentales:

- **Tiempo**: Intervalos de solicitudes, duración de sesión
- **Cabeceras**: Patrones User-Agent, cabeceras Accept faltantes, orden de cabeceras
- **Comportamiento**: Movimiento del ratón, ejecución de JavaScript, manejo de cookies
- **Navegación**: Acceso directo a endpoints, referer faltante
- **Contenido**: Velocidad de envío de formularios, activación de trampas canarias
- **Red**: Reputación IP, geolocalización

## 🛡️ Clasificación de Amenazas

### Clasificador Basado en Reglas

Usa heurísticas para detectar:
- Cabeceras HTTP faltantes (Accept, Accept-Language)
- Cadenas User-Agent similares a bots
- Firmas de agentes IA (GPT, Claude, LangChain)
- Patrones de escáneres de seguridad (Nikto, Burp)
- Acceso a trampas canarias
- Patrones de solicitudes sospechosamente rápidos

### Clasificador Bosque de Aislamiento

Usa detección de anomalías estadísticas para identificar patrones comportamentales inusuales.

### Clasificador de Conjunto

Combina múltiples clasificadores para detección robusta.

## 🤖 X Bot Detector

Módulo especializado para detectar bots y agentes IA en X (Twitter) con:

- **5 Analizadores**: Profile, Network, Temporal, Text, Behavior
- **31 Señales Comportamentales**: Patrones IA/LLM, métricas de engagement, análisis temporal
- **Modo Entrada Manual**: Funciona sin clave API de Twitter
- **Predicciones Explicables**: Cada clasificación incluye explicación detallada

### Ejemplo de Solicitud X Detector

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

## 📝 Ejemplo de Detección

```json
{
  "sessionId": "ABC123",
  "timestamp": "2024-03-07T10:30:00Z",
  "clientType": "AI_AGENT",
  "confidence": 0.85,
  "severity": "HIGH",
  "isThreat": true,
  "explanation": "User-Agent de agente IA detectado. Trampa canaria activada. Cabecera Accept faltante.",
  "triggeredFeatures": {
    "canary_trap": true,
    "ai_agent_user_agent": true,
    "missing_accept_header": true
  }
}
```

## 🤝 Contribuir

¡Las contribuciones son bienvenidas! Por favor, sigue estos pasos:

1. Hacer fork del repositorio
2. Crear una rama de características
3. Realizar tus cambios
4. Agregar pruebas
5. Enviar un pull request

## 📄 Licencia

Este proyecto está licenciado bajo la Licencia MIT.

## 👤 Autor

**Dania Ciampalini**

---

⚠️ **Nota**: Este es un sistema honeypot diseñado para investigación y monitoreo de seguridad. Despliega responsablemente y asegura el cumplimiento con las leyes y regulaciones pertinentes.

