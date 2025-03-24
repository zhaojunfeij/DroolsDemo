package com.example.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;

/**
 * HTTP请求工具类 - 使用Apache HttpClient实现
 */
@Component
public class HttpUtils {
    // 默认超时设置
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int SOCKET_TIMEOUT = 5000;
    private static final int CONNECTION_REQUEST_TIMEOUT = 5000;
    
    // 默认请求配置
    private static final RequestConfig DEFAULT_CONFIG = RequestConfig.custom()
            .setConnectTimeout(CONNECT_TIMEOUT)
            .setSocketTimeout(SOCKET_TIMEOUT)
            .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
            .build();

    /**
     * 发送GET请求
     *
     * @param urlString 请求URL
     * @return 响应字符串
     * @throws IOException IO异常
     */
    public static String get(String urlString) throws IOException {
        return get(urlString, null);
    }

    /**
     * 发送带请求头的GET请求
     *
     * @param urlString 请求URL
     * @param headers   请求头
     * @return 响应字符串
     * @throws IOException IO异常
     */
    public static String get(String urlString, Map<String, String> headers) throws IOException {
        // 创建HttpClient实例
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 创建GET请求
            HttpGet httpGet = new HttpGet(urlString);
            httpGet.setConfig(DEFAULT_CONFIG);
            
            // 设置请求头
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    if (StringUtils.hasText(entry.getValue())) {
                        httpGet.setHeader(entry.getKey(), entry.getValue());
                    }
                }
            }
            
            // 执行请求
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    throw new IOException("HTTP请求失败，响应码: " + statusCode);
                }
                
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    // 将响应内容转换为字符串
                    return EntityUtils.toString(entity, "UTF-8");
                }
                
                return "";
            }
        }
    }
} 