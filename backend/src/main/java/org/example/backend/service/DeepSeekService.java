package org.example.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek AI服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekService {
    
    @Value("${deepseek.api.key}")
    private String apiKey;
    
    @Value("${deepseek.api.url}")
    private String apiUrl;
    
    @Value("${deepseek.model}")
    private String model;
    
    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;
    
    /**
     * 调用DeepSeek API
     */
    public String chat(String prompt) {
        return chat(prompt, null);
    }
    
    /**
     * 调用DeepSeek API（带历史对话）
     */
    public String chat(String prompt, List<Map<String, String>> history) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 添加历史对话
            if (history != null && !history.isEmpty()) {
                messages.addAll(history);
            }
            
            // 添加当前消息
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);
            
            // 发送请求
            WebClient webClient = webClientBuilder.build();
            String response = webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            // 解析响应
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.path("choices").get(0)
                    .path("message").path("content").asText();
            
        } catch (Exception e) {
            log.error("调用DeepSeek API失败", e);
            throw new BusinessException("AI服务暂时不可用，请稍后再试");
        }
    }
    
    /**
     * 流式调用DeepSeek API
     */
    public Flux<String> chatStream(String prompt) {
        return chatStream(prompt, null);
    }
    
    /**
     * 流式调用DeepSeek API（带历史对话）
     */
    public Flux<String> chatStream(String prompt, List<Map<String, String>> history) {
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 添加历史对话
        if (history != null && !history.isEmpty()) {
            messages.addAll(history);
        }
        
        // 添加当前消息
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);
        
        // 构建请求体（启用流式）
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);
        requestBody.put("stream", true);  // 启用流式输出
        
        WebClient webClient = webClientBuilder.build();
        
        return webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> !line.isEmpty() && !line.equals("[DONE]"))
                .map(line -> {
                    try {
                        // 处理SSE格式的数据
                        String data = line;
                        if (line.startsWith("data: ")) {
                            data = line.substring(6);
                        }
                        if (data.equals("[DONE]")) {
                            return "";
                        }
                        JsonNode jsonNode = objectMapper.readTree(data);
                        JsonNode delta = jsonNode.path("choices").get(0).path("delta");
                        return delta.path("content").asText("");
                    } catch (Exception e) {
                        log.debug("解析流式数据: {}", line);
                        return "";
                    }
                })
                .filter(content -> !content.isEmpty());
    }
    
    /**
     * 流式AI答疑
     */
    public Flux<String> answerQuestionStream(String question, String courseContext) {
        String prompt = String.format(
                "你是一位专业的AI助教。课程背景信息：%s\n\n学生提问：%s\n\n请给出详细、准确的回答。",
                courseContext != null ? courseContext : "通用课程",
                question
        );
        return chatStream(prompt);
    }
    
    /**
     * AI答疑
     */
    public String answerQuestion(String question, String courseContext) {
        String prompt = String.format(
                "你是一位专业的AI助教。课程背景信息：%s\n\n学生提问：%s\n\n请给出详细、准确的回答。",
                courseContext != null ? courseContext : "通用课程",
                question
        );
        return chat(prompt);
    }
    
    /**
     * AI生成试题
     */
    public String generateExamQuestions(String knowledgePoint, String difficulty, int count) {
        String prompt = String.format(
                "请根据以下要求生成考试题目：\n知识点：%s\n难度：%s\n题目数量：%d\n\n" +
                "请生成包含选择题、判断题和简答题的试题，并以JSON格式返回，包含题目内容、选项、正确答案和解析。",
                knowledgePoint, difficulty, count
        );
        return chat(prompt);
    }
    
    /**
     * AI批改主观题
     */
    public Map<String, Object> gradeSubjectiveAnswer(String question, String correctAnswer, 
                                                      String studentAnswer, double totalScore) {
        String prompt = String.format(
                "作为AI阅卷老师，请评判以下主观题答案：\n\n题目：%s\n\n参考答案：%s\n\n学生答案：%s\n\n总分：%.1f\n\n" +
                "请以纯JSON格式返回评分结果（不要使用markdown代码块），包含：score（得分）、feedback（反馈意见）、keyPoints（答对的要点）、missedPoints（遗漏的要点）\n" +
                "注意：请直接返回JSON对象，不要用```json包裹",
                question, correctAnswer, studentAnswer, totalScore
        );
        
        try {
            String response = chat(prompt);
            // 清理可能的 Markdown 标记
            String cleanedResponse = cleanJsonFromMarkdown(response);
            return objectMapper.readValue(cleanedResponse, Map.class);
        } catch (Exception e) {
            log.error("AI批改失败", e);
            // 返回默认结果
            Map<String, Object> result = new HashMap<>();
            result.put("score", totalScore * 0.6);
            result.put("feedback", "AI评分失败，请人工复核");
            return result;
        }
    }
    
    /**
     * 清理 Markdown 代码块标记
     */
    private String cleanJsonFromMarkdown(String response) {
        if (response == null || response.isEmpty()) {
            return response;
        }
        
        String cleaned = response.trim();
        
        // 移除开头的 Markdown 代码块标记
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7).trim();
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3).trim();
        }
        
        // 移除结尾的 Markdown 代码块标记
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
        }
        
        // 查找第一个 { 和最后一个 }，提取JSON内容
        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');
        
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            cleaned = cleaned.substring(firstBrace, lastBrace + 1);
        }
        
        return cleaned.trim();
    }

    /**
     * AI批改报告
     */
    public Map<String, Object> reviewReport(String reportRequirement, String reportContent) {
        String prompt = String.format(
                "作为AI批改老师，请评审以下课程报告：\n\n报告要求：%s\n\n报告内容：%s\n\n" +
                "请从以下维度评分（满分100分）并以纯JSON格式返回（不要使用markdown代码块）：\n" +
                "1. relevanceScore（主题相关性，25分）\n" +
                "2. structureScore（逻辑结构，25分）\n" +
                "3. knowledgeScore（知识点覆盖，25分）\n" +
                "4. languageScore（语言规范性，25分）\n" +
                "5. totalScore（总分）\n" +
                "6. feedback（总体评语）\n" +
                "7. suggestions（改进建议，数组格式）\n" +
                "注意：请直接返回JSON对象，不要用```json包裹",
                reportRequirement, 
                reportContent.length() > 2000 ? reportContent.substring(0, 2000) + "..." : reportContent
        );
        
        try {
            String response = chat(prompt);
            log.debug("DeepSeek原始响应: {}", response);
            
            // 清理可能的 Markdown 标记
            String cleanedResponse = cleanJsonFromMarkdown(response);
            log.debug("清理后的JSON: {}", cleanedResponse);
            
            return objectMapper.readValue(cleanedResponse, Map.class);
        } catch (Exception e) {
            log.error("AI批改报告失败", e);
            // 返回默认结果
            Map<String, Object> result = new HashMap<>();
            result.put("totalScore", 70.0);
            result.put("relevanceScore", 18.0);
            result.put("structureScore", 17.0);
            result.put("knowledgeScore", 18.0);
            result.put("languageScore", 17.0);
            result.put("feedback", "AI评分失败，请人工复核");
            result.put("suggestions", List.of("建议人工复核"));
            return result;
        }
    }
    
    /**
     * AI整理笔记
     */
    public String organizeNotes(String rawNotes) {
        String prompt = String.format(
                "请帮助整理以下学习笔记，使其更加条理清晰、重点突出：\n\n%s\n\n" +
                "要求：\n1. 提取关键知识点\n2. 分类整理\n3. 标注重点难点\n4. 补充相关说明",
                rawNotes
        );
        return chat(prompt);
    }
    
    /**
     * 生成FAQ
     */
    public String generateFAQ(List<String> questions, List<String> answers) {
        StringBuilder sb = new StringBuilder("根据以下问答记录，生成常见问题FAQ：\n\n");
        for (int i = 0; i < questions.size() && i < answers.size(); i++) {
            sb.append(String.format("Q%d: %s\nA%d: %s\n\n", i+1, questions.get(i), i+1, answers.get(i)));
        }
        sb.append("请整理成结构化的FAQ，合并相似问题，优化答案表述。");
        return chat(sb.toString());
    }
}


