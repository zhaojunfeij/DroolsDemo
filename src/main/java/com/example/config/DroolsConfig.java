package com.example.config;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;

@Configuration
public class DroolsConfig {

    // 规则文件路径
    private static final String RULES_PATH = "rules/";

    @Bean
    public KieContainer kieContainer() throws IOException {
        KieServices kieServices = KieServices.Factory.get();

        // 创建KieFileSystem
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        // 加载规则文件
        loadRuleFiles(kieFileSystem);

        // 构建KieBuilder
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();

        // 检查是否有错误
        if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("规则文件编译错误: " + kieBuilder.getResults().toString());
        }

        // 创建KieContainer
        KieModule kieModule = kieBuilder.getKieModule();
        return kieServices.newKieContainer(kieModule.getReleaseId());
    }

    @Bean
    public KieBase kieBase() throws IOException {
        return kieContainer().getKieBase();
    }

    private void loadRuleFiles(KieFileSystem kieFileSystem) throws IOException {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        // 加载classpath下所有规则文件
        Resource[] resources = resourcePatternResolver.getResources("classpath*:" + RULES_PATH + "**/*.drl");

        for (Resource resource : resources) {
            // 为了调试，输出加载的规则文件
            System.out.println("加载规则文件: " + resource.getFilename());

            // 将规则文件添加到KieFileSystem
            String path = RULES_PATH + resource.getFilename();
            kieFileSystem.write(
                    ResourceFactory.newClassPathResource(path, this.getClass())
            );
        }
    }
}