package com.example.utils;

import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;

/**
 * 文件操作工具类
 */
@Component
public class FileUtils {
    
    /**
     * 写入DRL文件
     * 
     * @param content 文件内容
     * @param filePath 文件路径
     * @throws IOException IO异常
     */
    public void writeDrlFile(String content, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        }
    }
} 