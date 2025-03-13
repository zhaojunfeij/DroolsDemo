package com.example.parse;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * JSON流程规则转Drools DRL文件工具
 */
@Service
public class JsonToDrlConverter {

    private final String START_NODE = "start";
    private final String END_NODE = "end";
    private final String JUDGE_NODE = "judge";
    private final String COMPUTE_NODE = "compute";
    private final String ASSIGN_NODE = "assign";
    private final String RULE_NODE = "rule";

    private final Map<String, String> variableMap = new HashMap<>();

    {
        variableMap.put("a68e8ebd-3024-42d9-bbb9-14dac635e8fa", "userFund");
        variableMap.put("b6ef56c0-003a-4518-95d8-b93c927be38f", "orderAmt");
        variableMap.put("f8335c59-b1ee-4d50-8c64-5877e555a213", "storeId");
        variableMap.put("220db748-9fd9-405e-83a5-c24fb5de5f13", "businessId");
        variableMap.put("154782fd-71a6-4a65-889e-a5cb2101c1c1", "weComFriend");
        //variableMap.put("ee828ae9-ff51-45bc-bae9-6d3a7b13d634", "orderAmt");

    }

    /**
     * 主方法
     */
    private void main2(String[] args) {
        String inputJsonPath = "src/main/resources/fund_flow.json";
        String outputDrlPath = "src/main/resources/fund_flow_rules_V3.drl";
//        String inputJsonPath = "src/main/resources/qywx_flow.json";
//        String outputDrlPath = "src/main/resources/qywx_rules_V3.drl";
        try {
            String drlContent = convertJsonToDrl(inputJsonPath);
            writeDrlFile(drlContent, outputDrlPath);
            System.out.println("成功将JSON转换为DRL文件: " + outputDrlPath);
        } catch (Exception e) {
            System.err.println("转换失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 将JSON文件转换为DRL内容
     */
    public String convertJsonToDrl(String jsonFilePath) throws IOException {
        // 读取JSON文件
        File jsonFile = new File(jsonFilePath);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonFile);

        // 解析节点和边
        JsonNode nodesNode = rootNode.get("nodes");
        JsonNode edgesNode = rootNode.get("edges");

        // 构建节点映射和边映射
        Map<String, JsonNode> nodeMap = buildNodeMap(nodesNode);
        Map<String, List<EdgeInfo>> edgeMap = buildEdgeMap(edgesNode);

        // 找到开始节点
        String startNodeId = findNodeIdByType(nodeMap, START_NODE);
        if (startNodeId == null) {
            throw new IllegalArgumentException("未找到开始节点");
        }

        // 构建DRL内容
        StringBuilder drlBuilder = new StringBuilder();
        // 添加包声明和导入
        addPackageAndImports(drlBuilder);

        // 规则名称使用文件名
        String ruleName = new File(jsonFilePath).getName().replaceAll("\\.json$", "");
        drlBuilder.append("rule \"").append(ruleName).append("\"\n");
        drlBuilder.append("    agenda-group \"").append(ruleName).append("\"\n");
        drlBuilder.append("    no-loop true\n");
        drlBuilder.append("    when\n");
        drlBuilder.append("        $inputData : Map()\n");
        drlBuilder.append("    then\n");

        // 添加规则内容
        drlBuilder.append("        // 流程开始\n");
        drlBuilder.append("        System.out.println(\"开始执行规则流程: ").append(ruleName).append("\");\n");

        // 从开始节点遍历并生成规则内容
        buildRuleContent(drlBuilder, startNodeId, nodeMap, edgeMap, new HashSet<>());

        // 关闭规则
        drlBuilder.append("end\n");

        return drlBuilder.toString();
    }

    /**
     * 构建节点映射
     */
    private Map<String, JsonNode> buildNodeMap(JsonNode nodesNode) {
        Map<String, JsonNode> nodeMap = new HashMap<>();
        for (JsonNode node : nodesNode) {
            String id = node.get("id").asText();
            nodeMap.put(id, node);
        }
        return nodeMap;
    }

    /**
     * 寻找指定类型的节点ID
     */
    private String findNodeIdByType(Map<String, JsonNode> nodeMap, String nodeType) {
        for (Map.Entry<String, JsonNode> entry : nodeMap.entrySet()) {
            JsonNode node = entry.getValue();
            if (node.has("type") && node.get("type").asText().equalsIgnoreCase(nodeType)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 边信息类
     */
    class EdgeInfo {
        String sourceId;
        String targetId;
        int label;
        String caseId;

        public EdgeInfo(String sourceId, String targetId, int label, String caseId) {
            this.sourceId = sourceId;
            this.targetId = targetId;
            this.label = label;
            this.caseId = caseId;
        }

        public String getSourceId() {
            return sourceId;
        }

        public void setSourceId(String sourceId) {
            this.sourceId = sourceId;
        }

        public String getTargetId() {
            return targetId;
        }

        public void setTargetId(String targetId) {
            this.targetId = targetId;
        }

        public int getLabel() {
            return label;
        }

        public void setLabel(int label) {
            this.label = label;
        }

        public String getCaseId() {
            return caseId;
        }

        public void setCaseId(String caseId) {
            this.caseId = caseId;
        }
    }

    /**
     * 构建边映射
     */
    private Map<String, List<EdgeInfo>> buildEdgeMap(JsonNode edgesNode) {
        Map<String, List<EdgeInfo>> edgeMap = new HashMap<>();
        for (JsonNode edge : edgesNode) {
            String sourceId = edge.get("source").asText();
            String targetId = edge.get("target").asText();

            // 获取标签（用于判断条件分支）
            int label = edge.has("label") ? edge.get("label").asInt() : 0;

            // 获取决策用例ID
            String caseId = "";
            if (edge.has("properties") && edge.get("properties").has("decisionCase")) {
                caseId = edge.get("properties").get("decisionCase").asText();
            }

            // 添加到边映射
            if (!edgeMap.containsKey(sourceId)) {
                edgeMap.put(sourceId, new ArrayList<>());
            }
            edgeMap.get(sourceId).add(new EdgeInfo(sourceId, targetId, label, caseId));
        }
        return edgeMap;
    }

    /**
     * 添加包声明和导入
     */
    private void addPackageAndImports(StringBuilder drlBuilder) {
        drlBuilder.append("package org.example.ruleEngine.V3;\n\n");
        drlBuilder.append("import java.util.Map;\n");
        drlBuilder.append("import java.util.HashMap;\n");
        drlBuilder.append("import java.util.List;\n");
        drlBuilder.append("import java.util.ArrayList;\n");
        drlBuilder.append("import java.math.BigDecimal;\n");
        //drlBuilder.append("import com.rules.utils.HttpUtils;\n");
        drlBuilder.append("import com.example.utils.VariableUtils;\n\n");
    }

    /**
     * 递归构建规则内容
     */
    private void buildRuleContent(StringBuilder drlBuilder, String currentNodeId, Map<String, JsonNode> nodeMap, Map<String, List<EdgeInfo>> edgeMap, Set<String> visitedNodes) {

        // 避免循环
        if (visitedNodes.contains(currentNodeId)) {
            return;
        }
        visitedNodes.add(currentNodeId);

        JsonNode currentNode = nodeMap.get(currentNodeId);
        if (currentNode == null) {
            return;
        }

        String nodeType = currentNode.get("type").asText();

        // 根据节点类型处理
        switch (nodeType) {
            case START_NODE:
                processStartNode(drlBuilder, currentNode);
                break;
            case JUDGE_NODE:
                processJudgeNode(drlBuilder, currentNode, currentNodeId, nodeMap, edgeMap, visitedNodes);
                return; // 判断节点在内部处理下一个节点
            case COMPUTE_NODE:
                processComputeNode(drlBuilder, currentNode);
                break;
            case ASSIGN_NODE:
                processAssignNode(drlBuilder, currentNode);
                break;
            case RULE_NODE:
                processRuleNode(currentNode, nodeMap, edgeMap, drlBuilder);
                break;
            case END_NODE:
                processEndNode(drlBuilder, currentNode);
                break;
            default:
                drlBuilder.append("        // 未知节点类型: ").append(nodeType).append("\n");
        }

        // 处理下一个节点
        if (!END_NODE.equals(nodeType) && edgeMap.containsKey(currentNodeId)) {
            List<EdgeInfo> nextEdges = edgeMap.get(currentNodeId);
            if (!nextEdges.isEmpty()) {
                // 取第一个边（非判断节点只有一个出边）
                String nextNodeId = nextEdges.get(0).targetId;
                buildRuleContent(drlBuilder, nextNodeId, nodeMap, edgeMap, visitedNodes);
            }
        }
    }

    /**
     * 处理开始节点
     */
    private void processStartNode(StringBuilder drlBuilder, JsonNode node) {
        drlBuilder.append("        // 开始节点\n");
        drlBuilder.append("        Map<String, Object> flowContext = new HashMap<>();\n");
        drlBuilder.append("        flowContext.putAll($inputData);\n");
    }

    /**
     * 处理判断节点
     */
    private void processJudgeNode(StringBuilder drlBuilder, JsonNode node, String nodeId, Map<String, JsonNode> nodeMap, Map<String, List<EdgeInfo>> edgeMap, Set<String> visitedNodes) {

        drlBuilder.append("        // 判断节点\n");

        // 获取节点变量和关系
        JsonNode propertiesNode = node.get("properties");
        JsonNode variableListNode = propertiesNode.get("nodeVariableList");
        // 处理变量列表
        for (JsonNode variable : variableListNode) {
            buildVariable(drlBuilder, variable);
        }

        JsonNode relationShipGroupListNode = propertiesNode.get("relationShipGroupList");
        Map<String, Boolean> variableNameMap = new HashMap<>();//变量是否计算赋值，防止drools变量名重复
        // 处理判断逻辑
        if (relationShipGroupListNode != null && !relationShipGroupListNode.isEmpty()) {
            for (JsonNode relationShipGroup : relationShipGroupListNode) {
                JsonNode relationShipListNode = relationShipGroup.get("relationShipList");

                if (relationShipListNode != null && !relationShipListNode.isEmpty()) {
                    // 生成条件判断
                    drlBuilder.append("        // 条件分支\n");

                    // 查找对应的边
                    if (edgeMap.containsKey(nodeId)) {
                        List<EdgeInfo> edges = edgeMap.get(nodeId);
                        edges.sort(Comparator.comparingInt(EdgeInfo::getLabel));
                        for (EdgeInfo edge : edges) {
                            int conditionLabel = edge.label;
                            if (conditionLabel > 0) {
                                // 生成条件判断
                                generateConditionBranch(drlBuilder, relationShipListNode, conditionLabel, edge.targetId, nodeMap, edgeMap, visitedNodes, variableNameMap);
                            }
                        }
                    }
                }
            }
        } else {
            drlBuilder.append("        // 无条件判断，继续执行下一节点\n");
            // 处理下一个节点
            if (edgeMap.containsKey(nodeId)) {
                List<EdgeInfo> nextEdges = edgeMap.get(nodeId);
                if (!nextEdges.isEmpty()) {
                    String nextNodeId = nextEdges.get(0).targetId;
                    buildRuleContent(drlBuilder, nextNodeId, nodeMap, edgeMap, visitedNodes);
                }
            }
        }
    }

    /**
     * 生成条件分支
     */
    private void generateConditionBranch(StringBuilder drlBuilder, JsonNode relationShipListNode, int conditionLabel, String targetNodeId, Map<String, JsonNode> nodeMap, Map<String, List<EdgeInfo>> edgeMap, Set<String> visitedNodes, Map<String, Boolean> variableNameMap) {

        for (JsonNode relationship : relationShipListNode) {
            if (relationship.has("relationshipNo") && relationship.get("relationshipNo").asInt() == conditionLabel) {
                // 获取判断条件
                String variableNum = relationship.get("variableNo").asText();
                String variableNo = Objects.isNull(variableMap.get(variableNum)) ? variableNum : variableMap.get(variableNum);
                String operator = relationship.get("operator").asText();
                String operatorValue = relationship.get("operatorValue").asText();
                String operatorValueType = relationship.get("operatorValueType").asText();

                drlBuilder.append("        // 条件 ").append(conditionLabel).append("\n");
                //变量取值
                String variableConvert = variableNo + "Convert";
                if (!variableNameMap.containsKey(variableNo) || !variableNameMap.get(variableNo)) {
                    drlBuilder.append("        Object ").append(variableConvert).append("= ");

                    drlBuilder.append("VariableUtils.getVariableValue(flowContext, \"").append(variableNo).append("\");\n");
                    if (isNumeric(operatorValue)) {
                        drlBuilder.append("        BigDecimal ").append(variableNo).append("=new BigDecimal(").append(variableConvert).append(".toString());\n");
                    } else if (isBoolean(operatorValue)) {
                        drlBuilder.append("        Boolean ").append(variableNo).append("=(Boolean)").append(variableConvert).append(";\n");
                    } else {
                        drlBuilder.append("        String ").append(variableNo).append("=(String)").append(variableConvert).append(";\n");
                    }
                    drlBuilder.append("        System.out.println(\"开始计算变量取值");
                    drlBuilder.append(variableNo).append(":\"+").append(variableNo).append(");\n");
                    variableNameMap.put(variableNo, true);
                }
                // 构建条件表达式
                if ("data".equals(operatorValueType)) {
                    // 变量与常量比较
                    generateConditionExpression(drlBuilder, variableNo, operator, operatorValue);
                } else if ("var".equals(operatorValueType)) {
                    // 变量与变量比较
                    generateVariableComparisonExpression(drlBuilder, variableNo, operator, operatorValue);
                }

                drlBuilder.append(") {\n");

                // 递归处理目标节点
                buildRuleContent(drlBuilder, targetNodeId, nodeMap, edgeMap, new HashSet<>(visitedNodes));

                drlBuilder.append("        }\n");
            }
        }
    }

    /**
     * 生成条件表达式
     */
    private void generateConditionExpression(StringBuilder drlBuilder, String variableNo, String operator, String value) {
        // 检查是否是数字类型
        if (isNumeric(value) && !isEqOrNotEq(operator)) {
            buildNumberRuleExpress(drlBuilder, variableNo, operator, value);
        }
        // 检查是否是布尔类型
        else if (isBoolean(value)) {
            buildDefaultRuleExpress(drlBuilder, variableNo, operator, value);
        } else {
            buildDefaultRuleExpress(drlBuilder, variableNo, operator, value);
        }

    }

    private boolean isEqOrNotEq(String operator) {
        return "EQ".equals(operator) || "NOT_EQ".equals(operator);
    }

    private void buildDefaultRuleExpress(StringBuilder drlBuilder, String variableNo, String operator, String value) {
        drlBuilder.append("        if (").append(variableNo);
        switch (operator) {
            case "EQ":
                drlBuilder.append(" == ");
                break;
            case "NOT_EQ":
                drlBuilder.append(" != ");
                break;
            default:
                drlBuilder.append(" ").append(operator).append(" ");
        }
        drlBuilder.append(value);
        drlBuilder.append(")");

    }

    private void buildNumberRuleExpress(StringBuilder drlBuilder, String variableNo, String operator, String value) {
        drlBuilder.append("        if (").append(variableNo).append(".").append("compareTo(new BigDecimal(");
        drlBuilder.append(value);
        drlBuilder.append("))");
        switch (operator) {
            case "GT":
                drlBuilder.append(" > ");
                break;
            case "GE":
                drlBuilder.append(" >= ");
                break;
            case "LT":
                drlBuilder.append(" < ");
                break;
            case "LE":
                drlBuilder.append(" <= ");
                break;
            default:
                drlBuilder.append(" ").append(operator).append(" ");
        }
        drlBuilder.append("0");
    }

    /**
     * 生成变量比较表达式
     */
    private void generateVariableComparisonExpression(StringBuilder drlBuilder, String variable1, String operator, String variable2) {
        drlBuilder.append("VariableUtils.compareVariables(flowContext, \"").append(variable1).append("\", \"").append(operator).append("\", \"").append(variable2).append("\")");
    }

    /**
     * 处理计算节点
     */
    private void processComputeNode(StringBuilder drlBuilder, JsonNode node) {
        drlBuilder.append("        // 计算节点\n");

        // 获取节点变量和关系
        JsonNode propertiesNode = node.get("properties");
        if (propertiesNode.has("nodeVariableList") && propertiesNode.has("relationShipGroupList")) {
            JsonNode variableListNode = propertiesNode.get("nodeVariableList");
            JsonNode relationShipGroupListNode = propertiesNode.get("relationShipGroupList");

            // 处理变量列表
            for (JsonNode variable : variableListNode) {
                buildVariable(drlBuilder, variable);
            }

            // 处理关系列表
            for (JsonNode relationShipGroup : relationShipGroupListNode) {
                if (relationShipGroup.has("relationShipList")) {
                    JsonNode relationShipListNode = relationShipGroup.get("relationShipList");

                    for (JsonNode relationship : relationShipListNode) {
                        buildRelationShip(drlBuilder, relationship, node);

                    }
                }
            }
        }
    }

    private void buildRelationShip(StringBuilder drlBuilder, JsonNode relationship, JsonNode node) {
        String operator = relationship.get("operator").asText();
        String operatorValueNum = relationship.get("operatorValue").asText();
        String operatorValue = Objects.isNull(variableMap.get(operatorValueNum)) ? operatorValueNum : variableMap.get(operatorValueNum);
        String operatorValueType = relationship.get("operatorValueType").asText();
        String variableNum = relationship.get("variableNo").asText();
        String variableNo = Objects.isNull(variableMap.get(variableNum)) ? variableNum : variableMap.get(variableNum);
        drlBuilder.append("        // 操作: ").append(operator).append("\n");
        String nodeIdStr = node.get("id").asText();
        if (Objects.nonNull(relationship.get("relationshipNo"))) {
            nodeIdStr = nodeIdStr.concat("_" + relationship.get("relationshipNo").asText());
        }
        drlBuilder.append("        Object " + nodeIdStr).append("=");
        drlBuilder.append("VariableUtils.performOperation(flowContext, \"").append(variableNo).append("\", \"").append(operator).append("\", \"").append(operatorValue).append("\", \"").append(operatorValueType).append("\")").append(";\n");
        //节点
        drlBuilder.append("        flowContext.put(\"").append(nodeIdStr).append("\", ");
        drlBuilder.append(nodeIdStr);
        drlBuilder.append(");\n");

        drlBuilder.append("        System.out.println(\"变量计算结果");
        drlBuilder.append(nodeIdStr).append("_result:\"+");
        drlBuilder.append(nodeIdStr).append(");\n");
    }

    private void buildVariable(StringBuilder drlBuilder, JsonNode variable) {
        String variableName = variable.get("name").asText();
        String variableNum = variable.get("variableNo").asText();
        String variableNo = Objects.isNull(variableMap.get(variableNum)) ? variableNum : variableMap.get(variableNum);

        drlBuilder.append("        // 变量: ").append(variableName).append("\n");
        drlBuilder.append("        flowContext.put(\"").append(variableNo).append("\", ");

        // 如果有表达式树，处理表达式
        if (variable.has("data") && variable.get("data").has("expression_tree_json")) {
            drlBuilder.append("VariableUtils.evaluateExpression(").append("flowContext, ").append(JSON.toJSONString(variable.get("data").get("expression_tree_json").asText())).append(")");
        } else {
            drlBuilder.append("null");
        }
        drlBuilder.append(");\n");
        drlBuilder.append("        System.out.println(\"变量取值结果");
        drlBuilder.append(variableNo).append(":\"+flowContext.get(\"").append(variableNo).append("\"));\n");
    }

    /**
     * 处理规则节点
     */
    private void processRuleNode(JsonNode ruleNode, Map<String, JsonNode> nodeMap, Map<String, List<EdgeInfo>> edgeMap, StringBuilder drlBuilder) {
        String nodeId = ruleNode.get("id").asText();

        drlBuilder.append("       //规则节点: ").append(ruleNode.path("data").path("name").asText()).append("\n");

        // 获取节点变量
        JsonNode nodeVariables = ruleNode.path("properties").path("nodeVariableList");

        for (JsonNode variable : nodeVariables) {
            buildVariable(drlBuilder, variable);
        }

        // 获取关系判断条件
        JsonNode relationShipGroups = ruleNode.path("properties").path("relationShipGroupList");

        if (relationShipGroups.size() > 0) {
            // 处理AND/OR组合条件
            for (JsonNode group : relationShipGroups) {
                String groupOperator = group.has("operator") ? group.path("operator").asText() : "AND";
                JsonNode relationships = group.path("relationShipList");

                if (relationships.size() > 0) {
                    for (JsonNode relationship : relationships) {
                        buildRelationShip(drlBuilder, relationship, ruleNode);
                    }
                    drlBuilder.append("       // 条件组 (").append(groupOperator).append(")\n");
                    // 根据条件结果决定流程
                    drlBuilder.append("       if (");
                    for (JsonNode relationship : relationships) {
                        String nodeIdStr = ruleNode.get("id").asText();
                        if (Objects.nonNull(relationship.get("relationshipNo"))) {
                            nodeIdStr = nodeIdStr.concat("_" + relationship.get("relationshipNo").asText());
                        }
                        drlBuilder.append("flowContext.get(\"");
                        drlBuilder.append(nodeIdStr).append("\")");

                        String relationOperator = relationship.has("relationOperator") ? relationship.path("relationOperator").asText() : null;

                        if (relationOperator != null) {
                            if ("AND".equals(relationOperator)) {
                                drlBuilder.append(" && ");
                            } else if ("OR".equals(relationOperator)) {
                                drlBuilder.append(" || ");
                            }
                        }

                    }
                    drlBuilder.append("){\n");

                    //handleNodeTransition(nodeId, nodeMap, edgeMap, drlBuilder, indentLevel + 1);
                    drlBuilder.append("       } else {\n");
                    drlBuilder.append("       // 条件不满足，直接返回\n");
                    drlBuilder.append("       return;\n");
                    drlBuilder.append("       }\n");
                } else {
                    // 没有条件，直接过渡到下一个节点
                    //handleNodeTransition(nodeId, nodeMap, edgeMap, drlBuilder, indentLevel);
                }
            }
        } else {
            // 没有条件组，直接过渡到下一个节点
            //handleNodeTransition(nodeId, nodeMap, edgeMap, drlBuilder, indentLevel);
        }
    }

    /**
     * 处理赋值节点
     */
    private void processAssignNode(StringBuilder drlBuilder, JsonNode node) {
        drlBuilder.append("        // 赋值节点\n");

        // 获取节点变量和关系
        JsonNode propertiesNode = node.get("properties");
        if (propertiesNode.has("nodeVariableList") && propertiesNode.has("relationShipGroupList")) {
            JsonNode variableListNode = propertiesNode.get("nodeVariableList");
            JsonNode relationShipGroupListNode = propertiesNode.get("relationShipGroupList");

            // 处理变量列表
            for (JsonNode variable : variableListNode) {
                String variableName = variable.get("name").asText();
                String variableNo = variable.get("variableNo").asText();

                drlBuilder.append("        // 赋值变量: ").append(variableName).append("\n");
            }

            // 处理关系列表
            for (JsonNode relationShipGroup : relationShipGroupListNode) {
                if (relationShipGroup.has("relationShipList")) {
                    JsonNode relationShipListNode = relationShipGroup.get("relationShipList");

                    for (JsonNode relationship : relationShipListNode) {
                        String operator = relationship.get("operator").asText();

                        if ("SET_RESULT".equals(operator)) {
                            String variableNo = relationship.get("variableNo").asText();
                            String operatorValue = relationship.get("operatorValue").asText();
                            String operatorValueType = relationship.get("variableType").asText();

                            drlBuilder.append("        // 设置结果\n");
                            drlBuilder.append("        flowContext.put(\"").append(variableNo).append("\", ");

                            // 根据值类型处理 ,需要补充变量、节点 todo
                            // if (!"contextParams".equals(operatorValueType)) {
//                                if (isNumeric(operatorValue)) {
//                                    drlBuilder.append(operatorValue);
//                                } else {
                            drlBuilder.append("\"").append(operatorValue).append("\"");
                            //  }
//                            } else {
//                                drlBuilder.append("flowContext.get(\"").append(operatorValue).append("\")");
//                            }

                            drlBuilder.append(");\n");
                        }
                    }
                }
            }
        }
    }

    /**
     * 处理结束节点
     */
    private void processEndNode(StringBuilder drlBuilder, JsonNode node) {
        drlBuilder.append("        // 结束节点\n");

        // 获取结果设置
        JsonNode propertiesNode = node.get("properties");
        if (propertiesNode.has("resultSettingList")) {
            JsonNode resultSettingListNode = propertiesNode.get("resultSettingList");

            for (JsonNode resultSetting : resultSettingListNode) {
                String key = resultSetting.get("key").asText();
                String type = resultSetting.get("type").asText();
                String value = resultSetting.get("value").asText();

                drlBuilder.append("        // 设置结果: ").append(key).append("\n");

                switch (type) {
                    case "requestParams":
                        drlBuilder.append("        update($inputData); // 更新请求参数\n");
                        break;
                    case "node":
                        drlBuilder.append("        // 节点结果处理\n");
                        drlBuilder.append("        $inputData.put(\"").append(key).append("\", flowContext.get(\"").append(value + "_0").append("\"));\n");
                        break;
                    case "contextParams":
                        drlBuilder.append("        // 上下文参数处理\n");
                        drlBuilder.append("        $inputData.put(\"").append(key).append("\", flowContext.get(\"").append(value).append("\"));\n");
                        break;
                }
            }
        }

        drlBuilder.append("        System.out.println(\"规则流程执行完成\");\n");
    }

    /**
     * 检查字符串是否为数字
     */
    private boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 检查字符串是否为boolean
     */
    private boolean isBoolean(String str) {
        if (str == null) {
            return false;
        }
        try {
            // 转为小写，简化比较
            String lowerStr = str.toLowerCase().trim();

            // 检查是否为常见的布尔值表示
            return lowerStr.equals("true") || lowerStr.equals("false");

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 写入DRL文件
     */
    public void writeDrlFile(String content, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        }
    }
}
