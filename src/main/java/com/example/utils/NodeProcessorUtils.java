package com.example.utils;

import java.util.function.Predicate;

/**
 * 节点处理器工具类
 * 提供节点处理相关的通用工具方法
 */
public class NodeProcessorUtils {

    /**
     * 从类名获取Bean名称
     * Spring Bean通常是首字母小写的类名
     */
    public static String getBeanNameFromClassName(String className) {
        if (className == null || className.isEmpty()) {
            return "";
        }

        // 获取简单类名（不含包名）
        String simpleName = className;
        int lastDotIndex = className.lastIndexOf('.');
        if (lastDotIndex > 0) {
            simpleName = className.substring(lastDotIndex + 1);
        }

        // 首字母小写
        if (simpleName.length() > 1) {
            return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
        } else if (simpleName.length() == 1) {
            return simpleName.toLowerCase();
        }

        return simpleName;
    }

    /**
     * 检查字符串是否为数字
     * 使用函数式编程风格和更安全的异常处理
     */
    public static boolean isNumeric(String str) {
        return checkString(str, s -> {
            try {
                Double.parseDouble(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        });
    }

    /**
     * 检查字符串是否为boolean
     * 使用函数式编程风格提高代码一致性
     */
    public static boolean isBoolean(String str) {
        return checkString(str, s -> {
            String lowerStr = s.toLowerCase().trim();
            return lowerStr.equals("true") || lowerStr.equals("false");
        });
    }

    /**
     * 判断是否为等于或不等于操作符
     */
    public static boolean isEqOrNotEq(String operator) {
        return "EQ".equals(operator) || "NOT_EQ".equals(operator);
    }

    /**
     * 字符串校验的通用方法
     * 优化点：抽取共用逻辑，减少重复代码
     */
    private static boolean checkString(String str, Predicate<String> checker) {
        if (str == null) {
            return false;
        }
        try {
            return checker.test(str);
        } catch (Exception e) {
            return false;
        }
    }
} 