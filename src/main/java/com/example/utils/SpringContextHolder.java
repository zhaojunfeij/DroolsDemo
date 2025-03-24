package com.example.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring Context持有者，用于在非Spring管理的类中获取Bean
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {
    
    private static ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }
    
    /**
     * 获取ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    
    /**
     * 根据Bean名称获取Bean
     */
    public static Object getBean(String beanName) {
        if (applicationContext == null) {
            return null;
        }
        
        try {
            return applicationContext.getBean(beanName);
        } catch (BeansException e) {
            return null;
        }
    }
    
    /**
     * 根据类型获取Bean
     */
    public static <T> T getBean(Class<T> clazz) {
        if (applicationContext == null) {
            return null;
        }
        
        try {
            return applicationContext.getBean(clazz);
        } catch (BeansException e) {
            return null;
        }
    }
    
    /**
     * 根据Bean名称和类型获取Bean
     */
    public static <T> T getBean(String beanName, Class<T> clazz) {
        if (applicationContext == null) {
            return null;
        }
        
        try {
            return applicationContext.getBean(beanName, clazz);
        } catch (BeansException e) {
            return null;
        }
    }
} 