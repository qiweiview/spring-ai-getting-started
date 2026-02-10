package com.ylz.springaigettingstarted.tools;

import com.ylz.springaigettingstarted.utils.ZgjiaWeather;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.function.Consumer;

/**
 * AI工具类 - 演示 Spring AI 的工具调用(Tool Calling)功能
 *
 * 每个 @Tool 注解的方法都会被注册为可供AI模型调用的工具。
 * AI模型会根据工具的 description 自主决定何时调用哪个工具。
 *
 * 通过 eventCallback 回调，工具调用过程会实时推送到前端右侧面板展示。
 */
public class AITools {

    /**
     * 工具事件记录
     *
     * @param step    步骤标识
     * @param title   步骤标题
     * @param content 右侧面板摘要内容
     * @param detail  弹框中展示的详细代码/内容（为 null 时弹框显示 content）
     */
    public record ToolEvent(String step, String title, String content, String detail) {}

    private final Consumer<ToolEvent> eventCallback;

    public AITools(Consumer<ToolEvent> eventCallback) {
        this.eventCallback = eventCallback;
    }



    /**
     * 查询今天天气信息（真实数据，来源 zgjia.com）
     * 当用户询问 "北京天气怎么样" "上海今天下雨吗" 等天气相关问题时，AI会调用此工具
     */
    @Tool(description = "根据城市拼音查询该城市的今天实时天气信息，包括天气状况和温度等")
    public String getWeather(
            @ToolParam(description = "要查询天气的城市拼音（小写），如：beijing、shanghai、guangzhou、shenzhen、chengdu、hangzhou、wuhan、nanjing、chongqing、xian、fuzhou") String cityPinyin) {
        notifyEvent("tool_call", "工具调用",
                "📞 调用: getWeather(\"" + cityPinyin + "\")\n📋 描述: 查询城市今天天气信息",
                "// AI 模型自主决定调用工具\n"
                        + "Function Call: getWeather\n"
                        + "Arguments: {\n"
                        + "  \"cityPinyin\": \"" + cityPinyin + "\"\n"
                        + "}\n\n"
                        + "// 工具方法签名\n"
                        + "@Tool(description = \"根据城市拼音查询该城市的今天实时天气信息\")\n"
                        + "public String getWeather(String cityPinyin)");

        String result = ZgjiaWeather.todayWeather(cityPinyin);
        result = cityPinyin + " 今天天气: " + result;

        notifyEvent("tool_result", "工具返回", "📥 返回: " + result,
                "// 工具执行结果\n"
                        + "Tool: getWeather(\"" + cityPinyin + "\")\n"
                        + "Status: SUCCESS\n\n"
                        + "// 返回数据\n"
                        + result);
        return result;
    }

    /**
     * 查询明天天气信息（真实数据，来源 zgjia.com）
     * 当用户询问 "北京明天天气" "上海明天会下雨吗" 等明天天气相关问题时，AI会调用此工具
     */
    @Tool(description = "根据城市拼音查询该城市的明天天气预报信息，包括天气状况和温度等")
    public String getTomorrowWeather(
            @ToolParam(description = "要查询天气的城市拼音（小写），如：beijing、shanghai、guangzhou、shenzhen、chengdu、hangzhou、wuhan、nanjing、chongqing、xian、fuzhou") String cityPinyin) {
        notifyEvent("tool_call", "工具调用",
                "📞 调用: getTomorrowWeather(\"" + cityPinyin + "\")\n📋 描述: 查询城市明天天气信息",
                "// AI 模型自主决定调用工具\n"
                        + "Function Call: getTomorrowWeather\n"
                        + "Arguments: {\n"
                        + "  \"cityPinyin\": \"" + cityPinyin + "\"\n"
                        + "}\n\n"
                        + "// 工具方法签名\n"
                        + "@Tool(description = \"根据城市拼音查询该城市的明天天气预报信息\")\n"
                        + "public String getTomorrowWeather(String cityPinyin)");

        String result = ZgjiaWeather.tomorrowWeather(cityPinyin);
        result = cityPinyin + " 明天天气: " + result;

        notifyEvent("tool_result", "工具返回", "📥 返回: " + result,
                "// 工具执行结果\n"
                        + "Tool: getTomorrowWeather(\"" + cityPinyin + "\")\n"
                        + "Status: SUCCESS\n\n"
                        + "// 返回数据\n"
                        + result);
        return result;
    }

    private void notifyEvent(String step, String title, String content, String detail) {
        if (eventCallback != null) {
            try {
                eventCallback.accept(new ToolEvent(step, title, content, detail));
            } catch (Exception e) {
                // 忽略回调异常（可能客户端已断开连接）
            }
        }
    }
}
