package com.example.service;

import org.apache.commons.lang3.StringUtils;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RuleService {
    @Autowired
    private KieContainer kieContainer;

    /**
     * 执行规则引擎
     *
     * @param fact 传入的对象（事实）
     * @return 处理后的对象
     */
    public <T> T executeRule(T fact, String group) {
        KieSession kieSession = kieContainer.newKieSession();
        try {
            if (StringUtils.isNotBlank(group)) {
                kieSession.getAgenda().getAgendaGroup(group).setFocus();
            }
            // 插入对象
            kieSession.insert(fact);
            // 触发规则
            kieSession.fireAllRules();
            return fact;
        } finally {
            // 释放资源
            kieSession.dispose();
        }
    }
}
