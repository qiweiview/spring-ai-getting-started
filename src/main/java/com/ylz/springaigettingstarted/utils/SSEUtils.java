package com.ylz.springaigettingstarted.utils;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

public class SSEUtils {
    // 常量定义
    private static final long SSE_TIMEOUT = 10 * 60 * 1000L; // 10分钟

    public static SseEmitter createSseEmitter() {
        return new SseEmitter(SSE_TIMEOUT);
    }



    /**
     * 发送完成
     * @param emitter
     */
    public static void complete(SseEmitter emitter) {
        emitter.complete();
    }

    /**
     * 发送结束
     * @param emitter
     * @param data
     */
    public static void close(SseEmitter emitter, String data) {
        sendSseEvent(emitter, "close", data);
    }

    /**
     * 普通消息
     * @param emitter
     * @param data
     */
    public static void message(SseEmitter emitter, String data) {
        sendSseEvent(emitter, "message", data);
    }

    public static void finalResult(SseEmitter emitter, Object data) {
        sendSseEvent(emitter, "finalResult", data);
    }

    /**
     * 开始发送
     * @param emitter
     * @param data
     */
    public static void open(SseEmitter emitter, String data) {
        sendSseEvent(emitter, "open", data);
    }

    /**
     * 异常消息
     * @param emitter
     * @param data
     */
    public static void error(SseEmitter emitter, String data) {
       try {
           message(emitter,data);
           complete(emitter);
       }catch (Exception e){
           completeWithError(emitter,e);
       }

    }


    public static void completeWithError(SseEmitter emitter,Exception e) {
        emitter.completeWithError(e);
    }

    /**
     * 发送SSE事件 - 统一SSE发送逻辑
     */
    public static void sendSseEvent(SseEmitter emitter, String eventName, Object data) {
        try {
            SseEmitter.SseEventBuilder data1 = SseEmitter.event()
                    .name(eventName)
                    .data(data);

            emitter.send(data1);
        } catch (IOException e) {
            throw new RuntimeException("SSE发送失败:" + e.getMessage());
        }
    }


}
