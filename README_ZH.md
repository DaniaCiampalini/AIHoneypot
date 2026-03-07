# AIHoneypot - AI代理检测系统

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-green)
![Maven](https://img.shields.io/badge/Maven-Multi--Module-blue)
![License](https://img.shields.io/badge/License-MIT-blue)

一个复杂的蜜罐系统，用于使用行为指纹识别和机器学习来检测和分类AI代理、机器人和自动化HTTP客户端。

---

## 🌍 其他语言文档

- 🇬🇧 [English](README.md)
- 🇮🇹 [Italiano](README_IT.md)
- 🇫🇷 [Français](README_FR.md)
- 🇪🇸 [Español](README_ES.md)
- 🇩🇪 [Deutsch](README_DE.md)
- 🇨🇳 [简体中文](README_ZH.md) (当前)
- 🇯🇵 [日本語](README_JA.md)
- 🇷🇺 [Русский](README_RU.md)

---

## 🎯 特性

- **行为指纹识别**：分析HTTP请求模式以区分人类和机器人
- **多层分类**： 
  - 基于规则的已知模式检测
  - 用于异常检测的隔离森林
  - 结合多个分类器的集成方法
- **X（Twitter）机器人检测**：用于检测社交媒体上的机器人和AI代理的专门模块
- **金丝雀陷阱**：任何合法用户都不应访问的诱饵端点
- **实时威胁日志**：将威胁会话持久化到数据库
- **REST API仪表板**：监控和分析检测到的威胁
- **会话跟踪**：关联来自同一会话的多个请求

## 🏗️ 架构

这是一个Maven多模块项目，具有以下结构：

```
AIHoneypot/
├── core/              # 域模型、接口、异常
├── collector/         # 信号收集层（servlet过滤器）
├── analyzer/          # 威胁分类引擎
├── dashboard/         # REST API和统计信息
├── x-detector/        # X（Twitter）机器人检测模块
└── honeypot/          # 主Spring Boot应用程序 + 金丝雀陷阱
```

### 模块依赖关系

```
honeypot (主模块)
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

## 🚀 快速开始

### 先决条件

- Java 17+
- Maven 3.8+
- （可选）生产环境使用PostgreSQL

### 构建

```bash
mvn clean install
```

### 运行

```bash
cd honeypot
mvn spring-boot:run
```

应用程序将在 `http://localhost:8080` 上启动

### 访问仪表板

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2控制台**: http://localhost:8080/h2-console
- **API文档**: http://localhost:8080/api-docs

## 📊 核心域类

### Core模块（10个类）

1. **ClientType**（枚举）- HUMAN_BROWSER、AI_AGENT、BOT_SCRAPER等
2. **Severity**（枚举）- LOW、MEDIUM、HIGH、CRITICAL
3. **SignalType**（枚举）- 行为信号类型
4. **RawRequestSignals** - 原始HTTP请求数据
5. **ClassificationResult** - 威胁分类输出
6. **BehaviorSignalExtractor**（接口）- 特征提取契约
7. **ThreatClassifier**（接口）- 分类契约
8. **AIHoneypotException** - 基础异常
9. **SignalExtractionException** - 信号提取错误
10. **ClassificationException** - 分类错误

### Collector模块（3个类）

1. **SessionStore** - 内存会话跟踪
2. **HttpServletSignalExtractor** - 从servlet请求中提取信号
3. **SignalCollectorFilter** - 拦截所有请求的servlet过滤器

### Analyzer模块（4个类）

1. **RuleBasedClassifier** - 启发式威胁检测
2. **ThreatSession** - 持久化威胁的JPA实体
3. **ThreatSessionRepository** - Spring Data JPA仓库
4. **ThreatLogService** - 将威胁记录到数据库的服务

### Honeypot模块

1. **AIHoneypotApplication** - Spring Boot主类
2. **CanaryController** - 陷阱端点（/admin、/.env等）
3. **application.properties** - 配置文件

## 🕷️ 金丝雀陷阱端点

以下端点是触发立即威胁分类的陷阱：

- `/admin`、`/wp-admin` - 管理面板陷阱
- `/.env`、`/.env.local` - 环境文件陷阱
- `/config`、`/configuration` - 配置文件陷阱
- `/api/internal` - 内部API陷阱
- `/.git`、`/.svn` - 版本控制陷阱
- `/backup`、`/*.bak` - 备份文件陷阱
- `/*.sql`、`/dump.sql` - 数据库转储陷阱
- `/phpmyadmin` - PHPMyAdmin陷阱

## 📡 API端点

### 仪表板API

```
GET /api/dashboard/stats                  - 总体统计
GET /api/dashboard/threats/recent         - 最近威胁
GET /api/dashboard/threats/last-hours     - 时间范围内的威胁
GET /api/dashboard/stats/by-client-type   - 按客户端类型计数
GET /api/dashboard/stats/by-severity      - 按严重程度计数
GET /api/dashboard/stats/top-ips          - 主要攻击IP
GET /api/dashboard/health                 - 健康检查
```

### X检测器API

```
POST /api/x-detector/analyze/manual       - 分析X账户（手动输入）
GET  /api/x-detector/health               - 健康检查
GET  /api/x-detector/info                 - 系统信息
```

## 🔧 配置

编辑 `honeypot/src/main/resources/application.properties`：

```properties
# 数据库（开发用H2，生产用PostgreSQL）
spring.datasource.url=jdbc:h2:mem:honeypotdb

# 威胁检测阈值
honeypot.threat.confidence-threshold=0.5

# 启用金丝雀陷阱
honeypot.canary.enabled=true
```

## 🧪 测试

在所有模块上运行测试：

```bash
mvn test
```

## 📈 检测信号

系统分析这些行为信号：

- **时序**：请求间隔、会话持续时间
- **标头**：User-Agent模式、缺少Accept标头、标头顺序
- **行为**：鼠标移动、JavaScript执行、cookie处理
- **导航**：直接端点访问、缺少referer
- **内容**：表单提交速度、金丝雀陷阱触发
- **网络**：IP声誉、地理位置

## 🛡️ 威胁分类

### 基于规则的分类器

使用启发式方法检测：
- 缺少的HTTP标头（Accept、Accept-Language）
- 类似机器人的User-Agent字符串
- AI代理签名（GPT、Claude、LangChain）
- 安全扫描器模式（Nikto、Burp）
- 金丝雀陷阱访问
- 可疑的快速请求模式

### 隔离森林分类器

使用统计异常检测来识别不寻常的行为模式。

### 集成分类器

结合多个分类器进行稳健检测。

## 🤖 X机器人检测器

用于检测X（Twitter）上的机器人和AI代理的专门模块：

- **5个分析器**：Profile、Network、Temporal、Text、Behavior
- **31个行为信号**：AI/LLM模式、参与度指标、时间分析
- **手动输入模式**：无需Twitter API密钥即可工作
- **可解释的预测**：每个分类都包含详细说明

### X检测器请求示例

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

## 📝 检测示例

```json
{
  "sessionId": "ABC123",
  "timestamp": "2024-03-07T10:30:00Z",
  "clientType": "AI_AGENT",
  "confidence": 0.85,
  "severity": "HIGH",
  "isThreat": true,
  "explanation": "检测到AI代理User-Agent。触发金丝雀陷阱。缺少Accept标头。",
  "triggeredFeatures": {
    "canary_trap": true,
    "ai_agent_user_agent": true,
    "missing_accept_header": true
  }
}
```

## 🤝 贡献

欢迎贡献！请遵循以下步骤：

1. Fork仓库
2. 创建功能分支
3. 进行更改
4. 添加测试
5. 提交拉取请求

## 📄 许可证

该项目根据MIT许可证授权。

## 👤 作者

**Dania Ciampalini**

---

⚠️ **注意**：这是一个用于研究和安全监控的蜜罐系统。负责任地部署并确保遵守相关法律法规。

