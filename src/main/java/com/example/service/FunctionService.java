package com.example.service;

import com.alibaba.fastjson.JSON;
import com.example.model.FunctionResponse;
import com.example.model.FunctionResponse.FunctionInfo;
import com.example.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 函数服务类
 * 负责获取函数数据并支持动态添加
 */
@Service
public class FunctionService {
    private static final Logger log = LoggerFactory.getLogger(FunctionService.class);
    private static final String FUNCTION_API_URL = "https://api-internal.gaojihealth.cn/nyuwa/api/intranet/mdd/325/rule_function/list?function_type=&function_attr=&page=1&perPage=100&filter=function_name%2Ccs%2C&function_name=";
    
    // 函数代码映射，键为functionCode，值为FunctionInfo对象
    private static final Map<String, FunctionInfo> FUNCTION_CODE_MAP = new ConcurrentHashMap<>();
    
    /**
     * 服务启动时初始化函数代码映射
     */
    @PostConstruct
    public void init() {
        try {
            log.info("服务启动，开始初始化函数代码映射");
            loadFunctionCodes();
            
            // 创建测试函数信息
            FunctionInfo testInfo = new FunctionInfo();
            testInfo.setFunction_code("FUN_LOGIC_16989087308212");
            testInfo.setFunction_method_name("test");
            testInfo.setFunction_class_name("testBeanService");
            FUNCTION_CODE_MAP.put("FUN_LOGIC_16989087308212", testInfo);
        } catch (Exception e) {
            log.error("初始化函数代码映射失败", e);
        }
    }
    
    /**
     * 获取函数代码映射
     */
    public Map<String, FunctionInfo> getFunctionCodeMap() {
        return new HashMap<>(FUNCTION_CODE_MAP);
    }
    
    /**
     * 从API加载函数代码
     */
    private void loadFunctionCodes() throws IOException {
        log.info("开始从API加载函数代码映射");
        String response = HttpUtils.get(FUNCTION_API_URL);
        
        FunctionResponse functionResponse = JSON.parseObject(response, FunctionResponse.class);
        if (functionResponse != null && functionResponse.getStatus() != null && functionResponse.getStatus() == 0) {
            // 更新缓存
            if (functionResponse.getData() != null && functionResponse.getData().getRows() != null) {
                for (FunctionInfo info : functionResponse.getData().getRows()) {
                    FUNCTION_CODE_MAP.put(info.getFunction_code(), info);
                }
            }
            
            log.info("函数代码映射加载完成，共 {} 个函数", FUNCTION_CODE_MAP.size());
        } else {
            log.error("API请求失败：{}", functionResponse != null ? functionResponse.getMsg() : "无响应");
        }
    }
    
    /**
     * 动态添加函数代码映射
     * 
     * @param functionCode 函数代码
     * @param functionInfo 函数信息对象
     * @return 是否添加成功
     */
    public boolean addFunctionCode(String functionCode, FunctionInfo functionInfo) {
        if (functionCode == null || functionCode.trim().isEmpty() || 
            functionInfo == null || functionInfo.getFunction_method_name() == null || 
            functionInfo.getFunction_method_name().trim().isEmpty()) {
            log.warn("尝试添加无效的函数代码映射：code={}, info={}", functionCode, functionInfo);
            return false;
        }
        
        FUNCTION_CODE_MAP.put(functionCode, functionInfo);
        log.info("动态添加函数代码映射：code={}, methodName={}, className={}", 
            functionCode, functionInfo.getFunction_method_name(), functionInfo.getFunction_class_name());
        return true;
    }
    
    /**
     * 动态添加函数代码映射（简化版本）
     * 
     * @param functionCode 函数代码
     * @param functionMethodName 函数方法名
     * @param functionClassName 函数类名
     * @return 是否添加成功
     */
    public boolean addFunctionCode(String functionCode, String functionMethodName, String functionClassName) {
        if (functionCode == null || functionCode.trim().isEmpty() || 
            functionMethodName == null || functionMethodName.trim().isEmpty()) {
            log.warn("尝试添加无效的函数代码映射：code={}, method={}", functionCode, functionMethodName);
            return false;
        }
        
        FunctionInfo info = new FunctionInfo();
        info.setFunction_code(functionCode);
        info.setFunction_method_name(functionMethodName);
        info.setFunction_class_name(functionClassName);
        
        FUNCTION_CODE_MAP.put(functionCode, info);
        log.info("动态添加函数代码映射：code={}, method={}, class={}", 
            functionCode, functionMethodName, functionClassName);
        return true;
    }
    
    /**
     * 批量添加函数代码映射
     * 
     * @param mappings 函数代码映射
     * @return 添加的映射数量
     */
    public int addFunctionCodes(Map<String, FunctionInfo> mappings) {
        if (mappings == null || mappings.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        for (Map.Entry<String, FunctionInfo> entry : mappings.entrySet()) {
            if (addFunctionCode(entry.getKey(), entry.getValue())) {
                count++;
            }
        }
        
        log.info("批量添加函数代码映射完成，成功添加 {} 个", count);
        return count;
    }
    
    /**
     * 手动刷新函数代码映射
     */
    public void refreshFunctionCodes() throws IOException {
        log.info("手动刷新函数代码映射");
        // 清空当前映射
        FUNCTION_CODE_MAP.clear();
        // 重新加载
        loadFunctionCodes();
    }
} 