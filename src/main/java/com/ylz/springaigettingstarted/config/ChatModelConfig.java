package com.ylz.springaigettingstarted.config;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI模型配置类
 *
 * 通过 application.yml 中 "ai" 前缀的配置项自动绑定属性，
 * 并将 OpenAiApi、OpenAiChatModel 注册为 Spring Bean，
 * 供 ChatPageController 等组件注入使用。
 *
 * ⚠️ 请在 application.yml 中修改 ai.* 配置项
 */
@Configuration
@ConfigurationProperties(prefix = "ai")
public class ChatModelConfig {

    /** API密钥 */
    private String apiKey;

    /** API基础地址 */
    private String baseUrl;

    /** Chat Completions 接口路径 */
    private String completionsPath;

    /** 模型名称 */
    private String modelName;

    /** 温度参数 (0-1, 越高越随机) */
    private double temperature;

    /** 最大生成Token数 */
    private int maxTokens;

    /** 系统提示词 */
    private String systemPrompt;

    // ==================== Getter / Setter ====================

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getCompletionsPath() {
        return completionsPath;
    }

    public void setCompletionsPath(String completionsPath) {
        this.completionsPath = completionsPath;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    // ==================== Bean 定义 ====================

    @Bean
    public OpenAiApi openAiApi() {
        return OpenAiApi.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .completionsPath(completionsPath)
                .build();
    }

    @Bean
    public OpenAiChatModel chatModel(OpenAiApi openAiApi) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }
}
