# AIHoneypot - Sistema de Detección de Agentes IA

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-green)
![Maven](https://img.shields.io/badge/Maven-Multi--Module-blue)
![License](https://img.shields.io/badge/License-MIT-blue)

Un sofisticado sistema honeypot para detectar y clasificar agentes IA, bots y clientes HTTP automatizados utilizando huellas dactilares comportamentales y heurísticas basadas en reglas.

---

## Documentación en Otros Idiomas

- [English](../README.md)
- [Italiano](README_IT.md)
- [Español](README_ES.md) (Actual)
- [Deutsch](README_DE.md)

---

## Características

- **Panel de Control GUI Interactivo**: Hermosa aplicación de escritorio JavaFX con monitoreo en tiempo real
  - Estadísticas de amenazas en vivo con actualización automática
  - Gráficos interactivos (circular y de barras) para visualización de datos
  - Tabla de amenazas recientes con información detallada
  - Análisis de las IP atacantes principales
  - Tema moderno inspirado en iOS con animaciones suaves
  
- **Simulador de Tráfico**: Genera tráfico honeypot realista para pruebas
  - Generación automática de tráfico cada 5 segundos
  - Simulaciones de ataque cada 30 segundos
  - Ráfagas de tráfico cada 2 minutos para pruebas de estrés
  - Múltiples tipos de clientes (humanos, bots, agentes IA, escáneres)

- **Poblamiento de Base de Datos**: Pre-puebla la base de datos con datos históricos
  - ~270 sesiones de amenaza iniciales
  - 7 días de patrones históricos
  - Distribuciones de ataques realistas por severidad (incluyendo inyección SQL y picos de escaneo de bots)

- **Huella Digital Conductual**: Analiza patrones de solicitudes HTTP para distinguir humanos de bots

## Clasificación de Amenazas
- **Nivel 1: Clasificador Basado en Reglas**: Detección heurística basada en encabezados, tiempo y acceso a trampas (Decidible por construcción).
- **Nivel 2: Detección de Anomalías Isolation Forest**: ML no supervisado para detectar patrones de ataque desconocidos y cambios en la distribución (Caso de estudio de tesis).
- **Nivel 3: Detección por Conjunto (Ensemble)**: Integración de enfoques basados en reglas y ML para superar los límites teóricos individuales (Teorema de No Free Lunch).

---

## Tesis: Límites teóricos de los sistemas de ML para la seguridad
Este proyecto está estructurado como un caso de estudio para la tesis sobre los límites de la detección automática.
- **Escenarios de Prueba**:
  1. **Patrones Conocidos**: Eficacia del Clasificador Basado en Reglas.
  2. **Anomalías Desconocidas**: Eficacia de Isolation Forest en ataques de día cero.
  3. **Tráfico Borderline**: Análisis de falsos positivos/negativos y límites del aprendizaje PAC.
- **Integración con Weka**: Uso del paquete isolationForest para el análisis estadístico del tráfico.

- **Detección de Bots de X (Twitter)**: Módulo especializado para detectar bots y agentes IA en redes sociales
  - Análisis de perfil (edad, patrones de nombre de usuario, detección de bio)
  - Análisis de red (ratios seguidor/siguiendo)
  - Patrones temporales (frecuencia de publicación)
  - Detección de texto generado por IA
  - Puntuación explicable y recomendaciones

- **Trampas Canary**: Endpoints señuelo a los que ningún usuario legítimo debería acceder
  - /admin, /wp-admin
  - /.env
  - /config
  - /api/internal
  - /.git
  - /backup
  - Severidad CRÍTICA automática al acceder

- **Registro de Amenazas en Tiempo Real**: Persiste sesiones de amenazas en base de datos H2/PostgreSQL

- **Panel de Control API REST**: Monitorea y analiza amenazas detectadas a través de encabezados HTTP
  - Exportación a CSV: Exportación de datos para análisis experimental y evaluación de tesis

- **Seguimiento de Sesiones**: Correlaciona múltiples solicitudes de la misma sesión
  - Ingeniería de Características Avanzada: Seguimiento de tasas de solicitudes y tiempo promedio entre solicitudes

- **Análisis de Seguridad de Sitios Web (GUI)**:
  - Verificaciones SSL/HTTPS
  - Revisión de encabezados de seguridad
  - Puntuación heurística de vulnerabilidad y riesgo

---

## Arquitectura

Este es un proyecto Maven multi-módulo con la siguiente estructura:

```
AIHoneypot/
├── docs/              # Archivos README traducidos
├── scripts/           # Scripts de inicio
├── core/              # Modelos de dominio, interfaces, excepciones
├── collector/         # Capa de recolección de señales (filtros servlet)
├── analyzer/          # Motor de clasificación de amenazas
├── dashboard/         # API REST y estadísticas
├── gui/               # Panel de Control de escritorio JavaFX
├── x-detector         # Módulo de detección de bots X (Twitter)
└── honeypot/          # Aplicación Spring Boot principal + trampas canarias + simulador de tráfico
```

### Stack Tecnológico

- **Backend**: Spring Boot 3.2.2, Java 17
- **Frontend**: JavaFX 21 (GUI Desktop)
- **Database**: H2 (in-memory) / PostgreSQL (producción)
- **ML/AI**: Weka Isolation Forest, Heurísticas basadas en reglas, Métodos de Conjunto
- **Gráficos**: JavaFX Charts API
- **Build**: Maven Multi-Module

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

---

## Inicio Rápido

### Requisitos Previos

- Java 17+
- Maven 3.8+
- (Opcional) PostgreSQL para producción

### Opción 1: Inicio Completo (Backend + GUI) - RECOMENDADO

Inicia todo con un solo comando:

```bash
./scripts/start-complete.sh
```

Este script:
1. Verifica si el backend está en ejecución  
2. Construye el proyecto si es necesario  
3. Inicia el backend en segundo plano  
4. Espera a que el backend esté listo  
5. Lanza el Panel de Control GUI  

**Nota**: El backend se ejecuta en segundo plano. Los registros están en /tmp/aihoneypot-backend.log

### Opción 2: Inicio Manual

#### Iniciar Backend

```bash
cd honeypot
mvn spring-boot:run
```

La aplicación se iniciará en http://localhost:8080

#### Iniciar GUI (en otra terminal)

```bash
cd gui
mvn javafx:run
```

O usa el script solo de GUI:

```bash
./scripts/start-gui-only.sh
```

### Opción 3: Solo GUI (Backend ya en ejecución)

Si el backend ya está funcionando:

```bash
./scripts/start-gui-only.sh
```

Esto verifica si el backend está activo e inicia la GUI.

### Detener Backend

```bash
# Buscar y detener el proceso del backend
pkill -f 'spring-boot:run'

# O buscar el PID y detenerlo
lsof -ti:8080 | xargs kill -9
```

### Puntos de Acceso

- **Panel de Control GUI**: Se abre automáticamente al ejecutar ./scripts/start-complete.sh o ./scripts/start-gui-only.sh
- **Swagger de la API**: http://localhost:8080/swagger-ui.html
- **Consola H2**: http://localhost:8080/h2-console
- **Documentación de la API**: http://localhost:8080/api-docs
- **Control de Salud**: http://localhost:8080/actuator/health

---

## Clases de Dominio Core

### Módulo Core (10 clases)

1. **ClientType** (enum) - HUMAN_BROWSER, AI_AGENT, BOT_SCRAPER, SEARCH_ENGINE, SECURITY_SCANNER, UNKNOWN  
2. **Severity** (enum) - LOW, MEDIUM, HIGH, CRITICAL  
3. **SignalType** (enum) - Tipos de señales comportamentales  
4. **RawRequestSignals** - Datos de solicitud HTTP sin procesar  
5. **ClassificationResult** - Resultado de la clasificación de amenazas  
6. **BehaviorSignalExtractor** (interfaz) - Contrato de extracción de características  
7. **ThreatClassifier** (interfaz) - Contrato de clasificación  
8. **AIHoneypotException** - Excepción base  
9. **SignalExtractionException** - Errores de extracción de señales  
10. **ClassificationException** - Errores de clasificación  

### Módulo Collector (4 clases)

1. **SessionStore** - Seguimiento de sesiones en memoria  
2. **HttpServletSignalExtractor** - Extrae señales de solicitudes servlet
3. **SignalCollectorFilter** - Filtro servlet que intercepta todas las solicitudes
4. **IsolationForestAnomalyDetector** - Detección de anomalías no supervisada usando Weka

### Módulo Analyzer (5 clases)

1. **RuleBasedClassifier** - Detección heurística de amenazas  
2. **EnsembleClassifier** - Clasificador agregado (Nivel 3)
3. **ThreatSession** - Entidad JPA para amenazas persistidas  
4. **ThreatSessionRepository** - Repositorio Spring Data JPA  
5. **ThreatLogService** - Servicio para registro de amenazas en BD

### Módulo Honeypot

1. **AIHoneypotApplication** - Clase principal de Spring Boot  
2. **CanaryController** - Endpoints trampa  
3. **TrafficSimulator** - Tráfico realista y simulación de ataques
4. **DatabaseSeeder** - Población inicial de datos
5. **application.properties** - Configuración  

---

## Endpoints Trampa Canaria

Los siguientes endpoints son trampas que activan la clasificación inmediata de amenazas:

- /admin, /wp-admin  
- /.env  
- /config  
- /api/internal  
- /.git  
- /backup  

---

## Endpoints API

### API del Panel de Control

```
GET /api/dashboard/stats                  - Estadísticas generales
GET /api/dashboard/threats/recent         - Amenazas recientes
GET /api/dashboard/threats/last-hours     - Amenazas en rango de tiempo
GET /api/dashboard/stats/by-client-type   - Conteo por tipo de cliente
GET /api/dashboard/stats/by-severity      - Conteo por severidad
GET /api/dashboard/stats/top-ips          - IPs atacantes principales
GET /api/dashboard/export/csv             - Exportar amenazas a CSV
GET /api/dashboard/health                 - Control de salud
```

---

## Configuración

Editar honeypot/src/main/resources/application.properties:

```properties
# Base de datos (H2 para dev, PostgreSQL para prod)
spring.datasource.url=jdbc:h2:mem:honeypotdb

# Umbral de detección de amenazas
honeypot.threat.confidence-threshold=0.5

# Habilitar trampas canarias
honeypot.canary.enabled=true
```

---

## Pruebas

Ejecutar pruebas en todos los módulos:

```bash
mvn test
```

---

## Señales de Detección

El sistema analiza estas señales comportamentales:

- **Tiempo**: Intervalos de solicitudes, duración de sesión, tiempo promedio entre solicitudes  
- **Encabezados**: Patrones de User-Agent, encabezados Accept faltantes, orden de encabezados  
- **Comportamiento**: Ejecución de JavaScript, manejo de cookies (si está disponible)  
- **Navegación**: Acceso directo a endpoints, referer faltante, profundidad de la ruta  
- **Contenido**: Activación de trampas canarias, complejidad de la consulta, proporción de caracteres sospechosos  
- **Red**: Análisis de IP, tasa de solicitudes por minuto

---

## Clasificación de Amenazas

### Clasificador Basado en Reglas

Usa heurísticas para detectar:
- Encabezados HTTP faltantes (Accept, Accept-Language)
- Cadenas de User-Agent similares a bots
- Firmas de agentes IA (GPT, Claude, LangChain)
- Patrones de escáneres de seguridad (Nikto, Burp)
- Acceso a trampas canarias
- Patrones de solicitudes sospechosamente rápidos

### Detección de Anomalías Isolation Forest

Capa de aprendizaje automático no supervisada que:
- Aprende patrones de tráfico normal
- Detecta desviaciones (anomalías) sin conocimiento previo de tipos de ataque
- Utiliza 12 características comportamentales, incluyendo la entropía del tiempo y la estructura de la URI

### Clasificador de Conjunto (Ensemble)

Agrega múltiples capas de detección:
- Combina el Nivel 1 (Reglas) y el Nivel 2 (ML)
- Resuelve discrepancias entre clasificadores
- Implementa mitigaciones teóricas para el teorema de No Free Lunch

---

## Detector de Bots de X

Módulo especializado para detectar bots y agentes IA en X (Twitter) con análisis comportamental avanzado.

### Características

- **5 Analizadores Independientes**: Profile, Network, Temporal, Text, Behavior  
- **Predicciones Explicables**: Cada clasificación incluye una explicación detallada y las señales activadas  
- **Pesos Configurables**: Cada analizador tiene un peso ajustable en la puntuación final  
- **Modo de Entrada Manual**: Funciona sin clave de API de Twitter

### Desglose de Analizadores

| Analizador | Peso | Señales Clave |
|------------|------|---------------|
| **ProfileAnalyzer** | 20% | Edad de la cuenta, patrones de nombre de usuario, palabras clave de IA en la bio, imagen por defecto |
| **NetworkAnalyzer** | 25% | Ratio de seguidores/seguidos, spam de seguidores, anomalías de participación |
| **TemporalAnalyzer** | 25% | Frecuencia de publicación, intervalos regulares (CV), ráfagas de publicaciones |
| **TextAnalyzer** | 30% | Patrones de IA/LLM, repetición de texto, clientes de automatización, vocabulario |
| **BehaviorAnalyzer** | 20% | Ratio de retweets, sin respuestas, clientes de API, uniformidad de idioma |

### Niveles de Clasificación

| Rango de Puntuación | Clasificación | Acción |
|---------------------|---------------|---------|
| < 0.3 | **PROBABLEMENTE HUMANO** | Permitir |
| 0.3 - 0.6 | **INCIERTO** | Monitorear |
| 0.6 - 0.8 | **PROBABLE BOT** | Revisar |
| > 0.8 | **BOT CONFIRMADO** | Bloquear |

---

## Ejemplo de Detección

```json
{
  "sessionId": "ABC123",
  "timestamp": "2024-03-07T10:30:00Z",
  "clientType": "AI_AGENT",
  "confidence": 0.85,
  "severity": "HIGH",
  "isThreat": true,
  "explanation": "User-Agent de agente IA detectado. Trampa canaria activada. Encabezado Accept faltante.",
  "triggeredFeatures": {
    "canary_trap": true,
    "ai_agent_user_agent": true,
    "missing_accept_header": true
  }
}
```

---

## Roadmap

Los siguientes elementos están planificados para futuras iteraciones:

- **Ampliación de la cobertura de endpoints canarios** más allá de la lista core actual
- **Señales comportamentales más ricas** (movimiento del ratón, huella digital de JS avanzada)
- **Profundidad de análisis de seguridad extendida** con puntuación multi-etapa

---

## Contribuir

¡Las contribuciones son bienvenidas! Por favor, sigue estos pasos:

1. Haz un fork del repositorio  
2. Crea una rama de características  
3. Realiza tus cambios  
4. Agrega pruebas  
5. Envía una solicitud de pull request  

---

## Licencia

Este proyecto está licenciado bajo la Licencia MIT.

---

## Autor

**Dania Ciampalini**

---

**Nota**: Este es un sistema honeypot diseñado para investigación y monitoreo de seguridad. Despliega con responsabilidad y asegura el cumplimiento de las leyes y regulaciones pertinentes.
