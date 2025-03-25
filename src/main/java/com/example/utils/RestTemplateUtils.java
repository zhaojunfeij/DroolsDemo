package com.example.utils;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RestTemplate工具类
 * 提供REST调用的公共方法
 */
@Component
public class RestTemplateUtils {
    private static final Logger LOGGER = Logger.getLogger(RestTemplateUtils.class.getName());
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${rest.services.base-urls:#{null}}")
    private Map<String, String> serviceBaseUrls;
    
    /**
     * 获取服务基础URL
     * 
     * @param serviceName 服务名称
     * @return 服务基础URL
     */
    public String getServiceBaseUrl(String serviceName) {
        if (serviceBaseUrls != null && serviceBaseUrls.containsKey(serviceName)) {
            return serviceBaseUrls.get(serviceName);
        }
        return "http://" + serviceName;
    }
    
    /**
     * 执行GET请求
     * 
     * @param serviceName 服务名称
     * @param apiPath API路径
     * @param params 请求参数
     * @return 响应结果
     */
    public JSONObject executeGetRequest(String serviceName, String apiPath, Map<String, Object> params) {
        try {
            // 构建完整URL
            String baseUrl = getServiceBaseUrl(serviceName);
            String fullUrl = baseUrl + "/" + apiPath;
            
            // 构建带参数的URL
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fullUrl);
            if (params != null) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    builder.queryParam(entry.getKey(), entry.getValue());
                }
            }
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            
            // 执行请求
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<JSONObject> response = restTemplate.exchange(
                builder.toUriString(), 
                HttpMethod.GET, 
                entity, 
                JSONObject.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "GET请求失败: " + serviceName + "/" + apiPath, e);
            return null;
        }
    }
    
    /**
     * 执行POST请求
     * 
     * @param serviceName 服务名称
     * @param apiPath API路径
     * @param params 请求参数
     * @return 响应结果
     */
    public JSONObject executePostRequest(String serviceName, String apiPath, Map<String, Object> params) {
        try {
            // 构建完整URL
            String baseUrl = getServiceBaseUrl(serviceName);
            String fullUrl = baseUrl + "/" + apiPath;
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 构建请求体
            JSONObject requestBody = new JSONObject(params == null ? new HashMap<>() : params);
            HttpEntity<JSONObject> entity = new HttpEntity<>(requestBody, headers);
            
            // 执行请求
            ResponseEntity<JSONObject> response = restTemplate.exchange(
                fullUrl,
                HttpMethod.POST,
                entity,
                JSONObject.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "POST请求失败: " + serviceName + "/" + apiPath, e);
            return null;
        }
    }
} 