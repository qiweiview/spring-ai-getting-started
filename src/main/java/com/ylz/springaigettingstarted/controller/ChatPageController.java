package com.ylz.springaigettingstarted.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ylz.springaigettingstarted.config.ChatModelConfig;
import com.ylz.springaigettingstarted.tools.AITools;
import com.ylz.springaigettingstarted.utils.SSEUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AI对话页面控制器
 *
 * 提供两个端点:
 * - GET /             渲染Thymeleaf对话页面
 * - GET /chat/stream  SSE流式对话接口，返回实时处理事件和AI回复
 *
 * SSE事件类型说明:
 * - "process"  处理流程事件 (JSON) → 右侧面板展示模型加载、提示词组合、工具调用等
 * - "message"  AI回复文本Token     → 左侧对话面板流式展示
 * - "close"    对话完成信号         → 前端关闭SSE连接
 */
@Controller
public class ChatPageController {

    private static final long SSE_TIMEOUT = 10 * 60 * 1000L; // 10分钟
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final OpenAiChatModel chatModel;
    private final ChatModelConfig chatModelConfig;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public ChatPageController(OpenAiChatModel chatModel, ChatModelConfig chatModelConfig) {
        this.chatModel = chatModel;
        this.chatModelConfig = chatModelConfig;
    }

    /**
     * 渲染对话页面
     */
    @GetMapping("/")
    public String chatPage() {
        return "chat";
    }

    /**
     * SSE流式对话接口
     *
     * @param message 用户输入的消息
     * @return SseEmitter 服务端推送事件流
     */
    @GetMapping("/chat/stream")
    @ResponseBody
    public SseEmitter chatStream(@RequestParam String message) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        AtomicBoolean active = new AtomicBoolean(true);

        // 注册SSE生命周期回调，用于感知客户端断开
        emitter.onCompletion(() -> active.set(false));
        emitter.onTimeout(() -> active.set(false));
        emitter.onError(e -> active.set(false));

        // 在独立线程中执行流式对话，避免阻塞Servlet线程
        executor.execute(() -> doStreamChat(message, emitter, active));

        return emitter;
    }

    /**
     * 执行流式对话的核心逻辑 (在独立线程中运行)
     *
     * 流程: 模型初始化 → 提示词组合 → 注册工具 → 发送请求 → 接收流式响应 → 完成
     */
    private void doStreamChat(String message, SseEmitter emitter, AtomicBoolean active) {
        try {
            // ========== 步骤1: 模型初始化信息 ==========
            sendProcess(emitter, "model_init", "模型初始化",
                    String.format("模型: %s\n温度: %s\n最大Token: %d\nAPI地址: %s",
                            chatModelConfig.getModelName(),
                            chatModelConfig.getTemperature(),
                            chatModelConfig.getMaxTokens(),
                            chatModelConfig.getBaseUrl()),
                    // detail: 弹框中展示的代码
                    "// ChatModelConfig.java - 模型配置\n"
                            + "OpenAiChatOptions options = OpenAiChatOptions.builder()\n"
                            + "        .model(\"" + chatModelConfig.getModelName() + "\")\n"
                            + "        .temperature(" + chatModelConfig.getTemperature() + ")\n"
                            + "        .maxTokens(" + chatModelConfig.getMaxTokens() + ")\n"
                            + "        .build();\n\n"
                            + "OpenAiChatModel chatModel = OpenAiChatModel.builder()\n"
                            + "        .openAiApi(openAiApi)\n"
                            + "        .defaultOptions(options)\n"
                            + "        .build();\n\n"
                            + "// API 连接信息\n"
                            + "API_KEY  = \"sk-***" + chatModelConfig.getApiKey().substring(Math.max(0, chatModelConfig.getApiKey().length() - 4)) + "\"\n"
                            + "BASE_URL = \"" + chatModelConfig.getBaseUrl() + "\"");
            Thread.sleep(300); // 短暂延迟，让前端有时间展示动画

            // ========== 步骤2: 提示词组合展示 ==========
            sendProcess(emitter, "prompt_build", "提示词组合",
                    "[ System Prompt ]\n" + chatModelConfig.getSystemPrompt()
                            + "\n\n[ User Message ]\n" + message,
                    // detail: 弹框中展示完整 Prompt 结构
                    "// 完整 Prompt 结构\n"
                            + "{\n"
                            + "  \"messages\": [\n"
                            + "    {\n"
                            + "      \"role\": \"system\",\n"
                            + "      \"content\": \"" + chatModelConfig.getSystemPrompt().replace("\"", "\\\"") + "\"\n"
                            + "    },\n"
                            + "    {\n"
                            + "      \"role\": \"user\",\n"
                            + "      \"content\": \"" + message.replace("\"", "\\\"").replace("\n", "\\n") + "\"\n"
                            + "    }\n"
                            + "  ]\n"
                            + "}");
            Thread.sleep(200);

            // ========== 步骤3: 创建工具实例 (绑定SSE事件回调) ==========
            AITools tools = new AITools(event ->
                    sendProcess(emitter, event.step(), event.title(), event.content(), event.detail())
            );
            sendProcess(emitter, "tool_register", "注册工具",
                    "已注册以下工具供AI模型调用:\n"
                            + "  • getWeather - 查询城市天气信息",
                    // detail: 弹框中展示工具注册代码
                    "// 创建工具实例并注册到 ChatClient\n"
                            + "AITools tools = new AITools(eventCallback);\n\n"
                            + "// 已注册的工具方法:\n"
                            + "@Tool(description = \"根据城市拼音查询该城市的今天实时天气信息\")\n"
                            + "public String getWeather(String cityPinyin)\n\n"
                            + "@Tool(description = \"根据城市拼音查询该城市的明天天气预报信息\")\n"
                            + "public String getTomorrowWeather(String cityPinyin)\n\n"
                            + "// Spring AI 自动将 @Tool 方法转为 Function Calling 定义\n"
                            + "// AI 模型根据用户意图自主决定是否调用工具");
            Thread.sleep(200);

            // ========== 步骤4: 构建ChatClient (绑定模型+系统提示词+工具) ==========
            ChatClient client = ChatClient.builder(chatModel)
                    .defaultSystem(chatModelConfig.getSystemPrompt())
                    .defaultTools(tools)
                    .build();

            // ========== 步骤5: 发送API请求 ==========
            sendProcess(emitter, "api_call", "发送API请求",
                    String.format("请求地址: %s\n请求模型: %s\n流式输出: 是\n工具调用: 已启用",
                            chatModelConfig.getBaseUrl(),
                            chatModelConfig.getModelName()),
                    // detail: 弹框中展示请求构建代码
                    "// 构建 ChatClient 并发起流式请求\n"
                            + "ChatClient client = ChatClient.builder(chatModel)\n"
                            + "        .defaultSystem(SYSTEM_PROMPT)\n"
                            + "        .defaultTools(tools)\n"
                            + "        .build();\n\n"
                            + "// 流式调用\n"
                            + "client.prompt()\n"
                            + "        .user(\"" + message.replace("\"", "\\\"").replace("\n", "\\n") + "\")\n"
                            + "        .stream()\n"
                            + "        .content()     // Flux<String>\n"
                            + "        .subscribe();  // 开始接收 Token\n\n"
                            + "// HTTP 请求\n"
                            + "POST " + chatModelConfig.getBaseUrl() + "/api/v1/chat/completions\n"
                            + "Model: " + chatModelConfig.getModelName() + "\n"
                            + "Stream: true\n"
                            + "Tools: enabled");

            // ========== 步骤6: 接收并转发流式响应 ==========
            long startTime = System.currentTimeMillis();
            StringBuilder fullResponse = new StringBuilder();
            CountDownLatch latch = new CountDownLatch(1);

            sendProcess(emitter, "streaming", "接收流式响应", "等待模型响应...",
                    "// 流式响应处理\n"
                            + "client.prompt().user(message).stream().content()\n"
                            + "    .doOnNext(token -> {\n"
                            + "        // 每收到一个 Token，推送到前端\n"
                            + "        SSEUtils.sendSseEvent(emitter, \"message\", token);\n"
                            + "        fullResponse.append(token);\n"
                            + "    })\n"
                            + "    .doOnComplete(() -> {\n"
                            + "        // 流式响应结束\n"
                            + "        SSEUtils.close(emitter, \"done\");\n"
                            + "    })\n"
                            + "    .subscribe();\n\n"
                            + "// 实时 Token 流将显示在左侧对话面板");

            // 使用 ChatClient 的流式API
            // .stream().content() 返回 Flux<String>，每个元素是一个文本Token
            // ChatClient 会自动处理工具调用：检测→执行工具→重新请求→返回最终文本
            client.prompt()
                    .user(message)
                    .stream()
                    .content()
                    .takeWhile(token -> active.get())
                    .doOnNext(token -> {
                        try {
                            if (token != null && !token.isEmpty()) {
                                // 将Token推送到前端 (左侧对话面板)
                                SSEUtils.sendSseEvent(emitter, "message", token);
                                fullResponse.append(token);
                            }
                        } catch (Exception e) {
                            active.set(false);
                        }
                    })
                    .doOnError(error -> {
                        sendProcess(emitter, "error", "发生错误",
                                error.getClass().getSimpleName() + ": " + error.getMessage());
                        try {
                            SSEUtils.error(emitter, "AI响应异常: " + error.getMessage());
                        } catch (Exception e) {
                            // 忽略
                        }
                        latch.countDown();
                    })
                    .doOnComplete(() -> {
                        long duration = System.currentTimeMillis() - startTime;
                        sendProcess(emitter, "complete", "生成完成",
                                String.format("总耗时: %dms\n回复长度: %d字符",
                                        duration, fullResponse.length()));
                        try {
                            SSEUtils.close(emitter, "done");
                            SSEUtils.complete(emitter);
                        } catch (Exception e) {
                            // 忽略
                        }
                        latch.countDown();
                    })
                    .subscribe();

            // 等待流式响应完成（或超时）
            latch.await(SSE_TIMEOUT, TimeUnit.MILLISECONDS);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            sendProcess(emitter, "error", "线程中断", "对话处理被中断");
        } catch (Exception e) {
            sendProcess(emitter, "error", "发生异常",
                    e.getClass().getSimpleName() + ": "
                            + (e.getMessage() != null ? e.getMessage() : "未知错误"));
            try {
                SSEUtils.error(emitter, "服务异常: " + e.getMessage());
            } catch (Exception ex) {
                // 忽略
            }
        }
    }

    /**
     * 发送处理流程事件 (JSON格式) → 推送到前端右侧面板
     *
     * @param step    步骤标识 (model_init/prompt_build/tool_register/api_call/streaming/tool_call/tool_result/complete/error)
     * @param title   步骤标题
     * @param content 右侧面板摘要内容
     * @param detail  弹框中展示的详细代码/内容（传 null 时弹框显示 content）
     */
    private void sendProcess(SseEmitter emitter, String step, String title, String content, String detail) {
        try {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("step", step);
            event.put("title", title);
            event.put("content", content);
            if (detail != null) {
                event.put("detail", detail);
            }
            event.put("timestamp", System.currentTimeMillis());
            SSEUtils.sendSseEvent(emitter, "process", MAPPER.writeValueAsString(event));
        } catch (Exception e) {
            // 忽略发送失败 - 客户端可能已断开连接
        }
    }

    /** 不指定 detail 的简写形式，弹框默认显示 content */
    private void sendProcess(SseEmitter emitter, String step, String title, String content) {
        sendProcess(emitter, step, title, content, null);
    }
}
