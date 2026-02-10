/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ylz.springaigettingstarted.utils;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
public class ZgjiaWeather {

    public static String tomorrowWeather(String area) {
        if (null == area) {
            area = "beijing";
        }
        String url = "https://www.zgjia.com/%s/mingtian.html".formatted(area);

        // 1. 发起 HTTP 请求
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("User-Agent", "Mozilla/5.0 (compatible; WeatherBot/1.0)")
            .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            return "无法获取";
        }

        // 2. 使用 Jsoup 解析 HTML
        Document doc = Jsoup.parse(response.body());

        // 3. 提取 class="wendu-box" 的元素
        Element wenduBox = doc.selectFirst(".wendu-box");
        if (wenduBox != null) {
            return wenduBox.text();
        }
        return "无法获取";
    }

    public static String todayWeather(String area) {
        if (null == area) {
            area = "beijing";
        }
        String url = "https://www.zgjia.com/%s/".formatted(area);

        // 1. 发起 HTTP 请求
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("User-Agent", "Mozilla/5.0 (compatible; WeatherBot/1.0)")
            .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            return "无法获取";
        }

        // 2. 使用 Jsoup 解析 HTML
        Document doc = Jsoup.parse(response.body());

        // 3. 提取 class="wendu-box" 的元素
        Element wenduBox = doc.selectFirst(".wendu-box");
        if (wenduBox != null) {
            return wenduBox.text();
        }
        return "无法获取";
    }
}
