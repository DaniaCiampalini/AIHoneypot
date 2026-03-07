# AIHoneypot - AIエージェント検出システム

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-green)
![Maven](https://img.shields.io/badge/Maven-Multi--Module-blue)
![License](https://img.shields.io/badge/License-MIT-blue)

行動フィンガープリンティングと機械学習を使用して、AIエージェント、ボット、自動化されたHTTPクライアントを検出・分類する高度なハニーポットシステム。

---

## 🌍 他の言語のドキュメント

- 🇬🇧 [English](README.md)
- 🇮🇹 [Italiano](README_IT.md)
- 🇫🇷 [Français](README_FR.md)
- 🇪🇸 [Español](README_ES.md)
- 🇩🇪 [Deutsch](README_DE.md)
- 🇨🇳 [简体中文](README_ZH.md)
- 🇯🇵 [日本語](README_JA.md) (現在)
- 🇷🇺 [Русский](README_RU.md)

---

## 🎯 機能

- **行動フィンガープリンティング**：HTTPリクエストパターンを分析して人間とボットを区別
- **多層分類**： 
  - 既知のパターンに対するルールベースの検出
  - 異常検出のためのIsolation Forest
  - 複数の分類器を組み合わせたアンサンブル手法
- **X（Twitter）ボット検出**：ソーシャルメディア上のボットとAIエージェントを検出する専用モジュール
- **カナリアトラップ**：正当なユーザーがアクセスすべきでない囮エンドポイント
- **リアルタイム脅威ログ**：脅威セッションをデータベースに永続化
- **REST APIダッシュボード**：検出された脅威を監視・分析
- **セッション追跡**：同じセッションからの複数のリクエストを関連付け

## 🏗️ アーキテクチャ

以下の構造を持つMavenマルチモジュールプロジェクト：

```
AIHoneypot/
├── core/              # ドメインモデル、インターフェース、例外
├── collector/         # シグナル収集レイヤー（サーブレットフィルター）
├── analyzer/          # 脅威分類エンジン
├── dashboard/         # REST APIと統計
├── x-detector/        # X（Twitter）ボット検出モジュール
└── honeypot/          # メインSpring Bootアプリケーション + カナリアトラップ
```

### モジュール依存関係

```
honeypot (メイン)
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

## 🚀 クイックスタート

### 前提条件

- Java 17+
- Maven 3.8+
- （オプション）本番環境用のPostgreSQL

### ビルド

```bash
mvn clean install
```

### 実行

```bash
cd honeypot
mvn spring-boot:run
```

アプリケーションは `http://localhost:8080` で起動します

### ダッシュボードへのアクセス

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2コンソール**: http://localhost:8080/h2-console
- **APIドキュメント**: http://localhost:8080/api-docs

## 📊 コアドメインクラス

### Coreモジュール（10クラス）

1. **ClientType**（enum）- HUMAN_BROWSER、AI_AGENT、BOT_SCRAPERなど
2. **Severity**（enum）- LOW、MEDIUM、HIGH、CRITICAL
3. **SignalType**（enum）- 行動シグナルの種類
4. **RawRequestSignals** - 生のHTTPリクエストデータ
5. **ClassificationResult** - 脅威分類の出力
6. **BehaviorSignalExtractor**（インターフェース）- 特徴抽出契約
7. **ThreatClassifier**（インターフェース）- 分類契約
8. **AIHoneypotException** - 基本例外
9. **SignalExtractionException** - シグナル抽出エラー
10. **ClassificationException** - 分類エラー

### Collectorモジュール（3クラス）

1. **SessionStore** - メモリ内セッション追跡
2. **HttpServletSignalExtractor** - サーブレットリクエストからシグナルを抽出
3. **SignalCollectorFilter** - すべてのリクエストを傍受するサーブレットフィルター

### Analyzerモジュール（4クラス）

1. **RuleBasedClassifier** - ヒューリスティック脅威検出
2. **ThreatSession** - 永続化された脅威のJPAエンティティ
3. **ThreatSessionRepository** - Spring Data JPAリポジトリ
4. **ThreatLogService** - DBへの脅威ログサービス

### Honeypotモジュール

1. **AIHoneypotApplication** - Spring Bootメインクラス
2. **CanaryController** - トラップエンドポイント（/admin、/.envなど）
3. **application.properties** - 設定

## 🕷️ カナリアトラップエンドポイント

以下のエンドポイントは即座に脅威分類をトリガーするトラップです：

- `/admin`、`/wp-admin` - 管理パネルトラップ
- `/.env`、`/.env.local` - 環境ファイルトラップ
- `/config`、`/configuration` - 設定ファイルトラップ
- `/api/internal` - 内部APIトラップ
- `/.git`、`/.svn` - バージョン管理トラップ
- `/backup`、`/*.bak` - バックアップファイルトラップ
- `/*.sql`、`/dump.sql` - データベースダンプトラップ
- `/phpmyadmin` - PHPMyAdminトラップ

## 📡 APIエンドポイント

### ダッシュボードAPI

```
GET /api/dashboard/stats                  - 全体統計
GET /api/dashboard/threats/recent         - 最近の脅威
GET /api/dashboard/threats/last-hours     - 時間範囲内の脅威
GET /api/dashboard/stats/by-client-type   - クライアントタイプ別カウント
GET /api/dashboard/stats/by-severity      - 深刻度別カウント
GET /api/dashboard/stats/top-ips          - 主要攻撃IP
GET /api/dashboard/health                 - ヘルスチェック
```

### X検出器API

```
POST /api/x-detector/analyze/manual       - Xアカウント分析（手動入力）
GET  /api/x-detector/health               - ヘルスチェック
GET  /api/x-detector/info                 - システム情報
```

## 🔧 設定

`honeypot/src/main/resources/application.properties`を編集：

```properties
# データベース（開発用H2、本番用PostgreSQL）
spring.datasource.url=jdbc:h2:mem:honeypotdb

# 脅威検出閾値
honeypot.threat.confidence-threshold=0.5

# カナリアトラップを有効化
honeypot.canary.enabled=true
```

## 🧪 テスト

すべてのモジュールでテストを実行：

```bash
mvn test
```

## 📈 検出シグナル

システムは以下の行動シグナルを分析します：

- **タイミング**：リクエスト間隔、セッション期間
- **ヘッダー**：User-Agentパターン、Acceptヘッダーの欠落、ヘッダー順序
- **行動**：マウス移動、JavaScript実行、Cookie処理
- **ナビゲーション**：直接エンドポイントアクセス、refererの欠落
- **コンテンツ**：フォーム送信速度、カナリアトラップトリガー
- **ネットワーク**：IP評価、ジオロケーション

## 🛡️ 脅威分類

### ルールベース分類器

以下を検出するヒューリスティックを使用：
- 欠落したHTTPヘッダー（Accept、Accept-Language）
- ボットのようなUser-Agent文字列
- AIエージェントシグネチャ（GPT、Claude、LangChain）
- セキュリティスキャナーパターン（Nikto、Burp）
- カナリアトラップアクセス
- 疑わしいほど速いリクエストパターン

### Isolation Forest分類器

統計的異常検出を使用して異常な行動パターンを識別。

### アンサンブル分類器

堅牢な検出のために複数の分類器を組み合わせ。

## 🤖 Xボット検出器

X（Twitter）上のボットとAIエージェントを検出する専用モジュール：

- **5つのアナライザー**：Profile、Network、Temporal、Text、Behavior
- **31の行動シグナル**：AI/LLMパターン、エンゲージメントメトリクス、時間分析
- **手動入力モード**：Twitter APIキーなしで動作
- **説明可能な予測**：各分類に詳細な説明を含む

### X検出器リクエスト例

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

## 📝 検出例

```json
{
  "sessionId": "ABC123",
  "timestamp": "2024-03-07T10:30:00Z",
  "clientType": "AI_AGENT",
  "confidence": 0.85,
  "severity": "HIGH",
  "isThreat": true,
  "explanation": "AIエージェントUser-Agentを検出。カナリアトラップトリガー。Acceptヘッダー欠落。",
  "triggeredFeatures": {
    "canary_trap": true,
    "ai_agent_user_agent": true,
    "missing_accept_header": true
  }
}
```

## 🤝 貢献

貢献を歓迎します！以下の手順に従ってください：

1. リポジトリをフォーク
2. 機能ブランチを作成
3. 変更を加える
4. テストを追加
5. プルリクエストを送信

## 📄 ライセンス

このプロジェクトはMITライセンスの下でライセンスされています。

## 👤 著者

**Dania Ciampalini**

---

⚠️ **注意**：これは研究とセキュリティ監視のために設計されたハニーポットシステムです。責任を持って展開し、関連する法律と規制への準拠を確保してください。

