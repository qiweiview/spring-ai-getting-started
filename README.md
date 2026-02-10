# Spring AI Getting Started

基于 **Spring Boot 3.4** + **Spring AI 1.1** 的 AI 对话演示项目，支持 SSE 流式输出和 Function Calling（工具调用）。

---

## 快速开始

### 环境要求

- **JDK** 17+
- **Maven** 3.8+

### 1. 克隆项目

```bash
git clone <仓库地址>
cd spring-ai-getting-started
```

### 2. 配置 AI 模型参数

打开 `src/main/resources/application.yml`，修改 `ai` 下的配置项：

```yaml
ai:
  # API 密钥 —— 从你的 AI 服务提供商获取
  api-key: your-api-key-here

  # API 基础地址 —— 你的 AI 服务地址
  base-url: https://api.example.com

  # Chat Completions 接口路径（兼容 OpenAI 格式）
  completions-path: /v1/chat/completions

  # 模型名称 —— 请确保使用支持 Chat Completions 的模型
  model-name: gpt-4o

  # 温度参数 (0~1) —— 越高回答越随机/有创意，越低回答越确定/稳定
  temperature: 0.7

  # 最大生成 Token 数 —— 控制单次回复的最大长度
  max-tokens: 2048

  # 系统提示词 —— 定义 AI 的角色和行为
  system-prompt: >-
    你是一个有用的AI助手。你可以回答各种问题，并使用提供的工具获取实时信息。
    如果用户询问当前时间或日期，请使用 getCurrentDateTime 工具。
    如果用户询问天气情况，请使用 getWeather 工具。
    请用中文回答用户的问题。
```

#### 配置项说明

| 配置项 | 说明 | 示例值 |
|---|---|---|
| `ai.api-key` | API 密钥，从 AI 服务提供商处获取 | `sk-xxxxxxxxxxxxxxxx` |
| `ai.base-url` | API 服务的基础地址 | `https://api.openai.com` |
| `ai.completions-path` | Chat Completions 接口路径 | `/v1/chat/completions` |
| `ai.model-name` | 使用的模型名称 | `gpt-4o`、`gpt-3.5-turbo`、`deepseek-chat` |
| `ai.temperature` | 温度参数，范围 0~1 | `0.7` |
| `ai.max-tokens` | 单次回复最大 Token 数 | `2048` |
| `ai.system-prompt` | 系统提示词，定义 AI 角色和行为 | 见上方示例 |

> **提示**：本项目使用兼容 OpenAI API 格式的接口，因此可以对接任何兼容 OpenAI Chat Completions API 的服务（如 OpenAI、DeepSeek、智谱 AI、本地 Ollama 等）。

#### 常见 AI 服务配置示例

**OpenAI**
```yaml
ai:
  api-key: sk-xxxxxxxxxxxxxxxx
  base-url: https://api.openai.com
  completions-path: /v1/chat/completions
  model-name: gpt-4o
```

**DeepSeek**
```yaml
ai:
  api-key: sk-xxxxxxxxxxxxxxxx
  base-url: https://api.deepseek.com
  completions-path: /v1/chat/completions
  model-name: deepseek-chat
```

**本地 Ollama**
```yaml
ai:
  api-key: ollama  # Ollama 不校验 key，随意填写
  base-url: http://localhost:11434
  completions-path: /v1/chat/completions
  model-name: qwen2.5:7b
```

### 3. 启动项目

```bash
mvn spring-boot:run
```

### 4. 访问应用

打开浏览器访问 [http://localhost:8080](http://localhost:8080)，即可开始 AI 对话。

---

## 项目结构

```
src/main/
├── java/com/ylz/springaigettingstarted/
│   ├── config/
│   │   └── ChatModelConfig.java      # AI 模型配置（从 application.yml 读取）
│   ├── controller/
│   │   └── ChatPageController.java    # 对话页面 & SSE 流式接口
│   ├── tools/
│   │   └── AITools.java               # AI 可调用的工具（天气查询等）
│   └── utils/
│       ├── SSEUtils.java              # SSE 推送工具类
│       └── ZgjiaWeather.java          # 天气数据获取
└── resources/
    ├── application.yml                # 应用配置（AI 模型参数在此修改）
    └── templates/
        └── chat.html                  # Thymeleaf 对话页面
```

## 技术栈

- **Spring Boot** 3.4.2
- **Spring AI** 1.1.2（OpenAI 兼容）
- **Thymeleaf** — 页面模板引擎
- **SSE (Server-Sent Events)** — 流式响应推送
- **Lombok** — 简化 Java 代码
