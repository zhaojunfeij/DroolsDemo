package com.example.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 变量服务
 * 负责从远程API获取变量信息并处理为映射关系
 */
@Service
public class VariableService {
    private static final Logger log = LoggerFactory.getLogger(VariableService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${variable.api.url:https://api-internal.gaojihealth.cn/nyuwa/api/intranet/mdd/325/variable_info/list}")
    private String variableApiUrl;

    // 变量映射缓存，改用线程安全的ConcurrentHashMap
    private final Map<String, String> variableMap = new ConcurrentHashMap<>();

    /**
     * 服务初始化时加载变量映射
     */
    @PostConstruct
    public void init() {
        loadVariableMap();
    }

    /**
     * 从远程API加载变量映射
     */
    public void loadVariableMap() {
        try {
            String url = variableApiUrl + "?type=&page=1&perPage=200&name=&filter=name%2Ccs%2C&status=0";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JSONObject jsonResponse = JSON.parseObject(response.getBody());
                parseVariableResponse(jsonResponse);
                log.info("成功加载{}个变量映射", variableMap.size());
            }
        } catch (Exception e) {
            log.error("加载变量映射失败: {}", e.getMessage());
        }
    }

    /**
     * 解析变量响应数据 - 适配variable_data.json的格式
     * 采用函数式编程风格，简洁优雅地处理JSON数据
     */
    private void parseVariableResponse(JSONObject jsonResponse) {
        Map<String, String> newVariableMap = Optional.ofNullable(jsonResponse)
            .map(json -> json.getJSONObject("data"))
            .map(data -> data.getJSONArray("rows"))
            .map(this::extractVariableMappings)
            .orElseGet(HashMap::new);
            
        // 更新缓存
        variableMap.clear();
        variableMap.putAll(newVariableMap);
    }
    
    /**
     * 从数据数组中提取变量映射
     */
    private Map<String, String> extractVariableMappings(JSONArray dataArray) {
        return IntStream.range(0, dataArray.size())
            .mapToObj(dataArray::getJSONObject)
            .map(this::extractVariableEntry)
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(
                SimpleEntry::getKey,
                SimpleEntry::getValue,
                (v1, v2) -> v1  // 如果有重复键，保留第一个
            ));
    }
    
    /**
     * 从单个变量信息对象中提取键值对
     */
    private SimpleEntry<String, String> extractVariableEntry(JSONObject variableInfo) {
        String variableNo = variableInfo.getString("variable_no");
        if (variableNo == null) {
            return null;
        }
        
        return Optional.ofNullable(variableInfo.getString("expression_tree_json"))
            .map(this::parseExpressionTree)
            .map(value -> new SimpleEntry<>(variableNo, value))
            .orElse(null);
    }
    
    /**
     * 解析表达式树JSON，提取值
     */
    private String parseExpressionTree(String expressionJson) {
        try {
            JSONObject expressionObj = JSON.parseObject(expressionJson);
            
            return Optional.of(expressionObj)
                .filter(expr -> "SET_RESULT".equals(expr.getString("code")))
                .map(expr -> expr.getJSONArray("params"))
                .filter(params -> params != null && !params.isEmpty())
                .map(params -> params.getJSONObject(0))
                .map(param -> param.getString("code"))
                .filter(code -> code != null && code.startsWith("$."))
                .map(code -> code.substring(2))
                .orElse(null);
        } catch (Exception e) {
            log.debug("解析表达式失败: {}", expressionJson);
            return null;
        }
    }

    /**
     * 获取变量映射
     */
    public Map<String, String> getVariableMap() {
        return new HashMap<>(variableMap);
    }
    
    /**
     * 手动添加变量映射
     * 
     * @param variableNo 变量编号
     * @param variableValue 变量值
     * @return 是否添加成功
     */
    public boolean addVariable(String variableNo, String variableValue) {
        if (variableNo == null || variableNo.trim().isEmpty() || 
            variableValue == null || variableValue.trim().isEmpty()) {
            log.warn("尝试添加无效的变量映射：variableNo={}, value={}", variableNo, variableValue);
            return false;
        }
        
        variableMap.put(variableNo, variableValue);
        log.info("动态添加变量映射：variableNo={}, value={}", variableNo, variableValue);
        return true;
    }
    
    /**
     * 批量添加变量映射
     * 
     * @param mappings 变量映射
     * @return 添加的映射数量
     */
    public int addVariables(Map<String, String> mappings) {
        if (mappings == null || mappings.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            if (addVariable(entry.getKey(), entry.getValue())) {
                count++;
            }
        }
        
        log.info("批量添加变量映射完成，成功添加 {} 个", count);
        return count;
    }
    
    /**
     * 移除变量映射
     * 
     * @param variableNo 变量编号
     * @return 是否移除成功
     */
    public boolean removeVariable(String variableNo) {
        if (variableNo == null || variableNo.trim().isEmpty()) {
            return false;
        }
        
        String removed = variableMap.remove(variableNo);
        if (removed != null) {
            log.info("移除变量映射：variableNo={}", variableNo);
            return true;
        }
        return false;
    }
    
    /**
     * 手动刷新变量映射
     */
    public void refreshVariables() {
        log.info("手动刷新变量映射");
        // 清空当前映射
        variableMap.clear();
        // 重新加载
        loadVariableMap();
    }
} 