package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.Exam;
import org.example.backend.entity.ExamQuestion;
import org.example.backend.exception.BusinessException;
import org.example.backend.repository.ExamQuestionRepository;
import org.example.backend.repository.ExamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 考试服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamService {
    
    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final DeepSeekService deepSeekService;
    
    /**
     * 创建考试
     */
    @Transactional
    public Exam createExam(Exam exam) {
        return examRepository.save(exam);
    }
    
    /**
     * 更新考试
     */
    @Transactional
    public Exam updateExam(Long id, Exam exam) {
        Exam existing = getExamById(id);
        existing.setExamName(exam.getExamName());
        existing.setExamType(exam.getExamType());
        existing.setDescription(exam.getDescription());
        existing.setTotalScore(exam.getTotalScore());
        existing.setDuration(exam.getDuration());
        existing.setStartTime(exam.getStartTime());
        existing.setEndTime(exam.getEndTime());
        existing.setAntiCheat(exam.getAntiCheat());
        existing.setStatus(exam.getStatus());
        return examRepository.save(existing);
    }
    
    /**
     * 删除考试
     */
    @Transactional
    public void deleteExam(Long id) {
        examRepository.deleteById(id);
    }
    
    /**
     * 获取考试详情
     */
    public Exam getExamById(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new BusinessException("考试不存在"));
    }
    
    /**
     * 获取课程的所有考试
     */
    public List<Exam> getCourseExams(Long courseId) {
        return examRepository.findByCourseId(courseId);
    }
    
    /**
     * AI自动生成试题
     */
    @Transactional
    public List<ExamQuestion> generateQuestions(Long examId, String knowledgePoint, 
                                                  String difficulty, Integer count) {
        Exam exam = getExamById(examId);
        
        // 调用AI生成试题
        log.info("开始AI生成试题，考试ID: {}, 知识点: {}, 难度: {}, 数量: {}", 
                examId, knowledgePoint, difficulty, count);
        String aiResponse = deepSeekService.generateExamQuestions(knowledgePoint, difficulty, count);
        
        // 解析AI返回的JSON并创建试题
        try {
            // 使用ObjectMapper解析JSON
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            
            // 尝试解析为JSON数组
            com.fasterxml.jackson.databind.JsonNode rootNode;
            try {
                rootNode = objectMapper.readTree(aiResponse);
            } catch (Exception e) {
                // 如果直接解析失败，尝试提取JSON部分
                int jsonStart = aiResponse.indexOf("[");
                int jsonEnd = aiResponse.lastIndexOf("]");
                if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    String jsonPart = aiResponse.substring(jsonStart, jsonEnd + 1);
                    rootNode = objectMapper.readTree(jsonPart);
                } else {
                    throw new BusinessException("AI返回的内容无法解析为JSON: " + aiResponse);
                }
            }
            
            // 如果是数组，遍历创建试题
            if (rootNode.isArray()) {
                int order = 1;
                for (com.fasterxml.jackson.databind.JsonNode questionNode : rootNode) {
                    ExamQuestion question = new ExamQuestion();
                    question.setExamId(examId);
                    question.setQuestionType(questionNode.has("type") ? 
                            questionNode.get("type").asText() : "SINGLE_CHOICE");
                    question.setContent(questionNode.has("content") ? 
                            questionNode.get("content").asText() : "");
                    question.setOptions(questionNode.has("options") ? 
                            questionNode.get("options").toString() : null);
                    question.setCorrectAnswer(questionNode.has("correctAnswer") ? 
                            questionNode.get("correctAnswer").asText() : "");
                    question.setScore(questionNode.has("score") ? 
                            new java.math.BigDecimal(questionNode.get("score").asText()) : 
                            new java.math.BigDecimal("5.0"));
                    question.setDifficulty(getDifficultyLevel(difficulty));
                    question.setKnowledgePoint(knowledgePoint);
                    question.setAnalysis(questionNode.has("analysis") ? 
                            questionNode.get("analysis").asText() : "");
                    question.setQuestionOrder(order++);
                    question.setAiGenerated(1);
                    
                    examQuestionRepository.save(question);
                    log.info("保存AI生成的试题: {}", question.getContent().substring(0, 
                            Math.min(30, question.getContent().length())));
                }
            }
            
            log.info("AI试题生成完成，共生成 {} 道题", rootNode.size());
        } catch (Exception e) {
            log.error("解析AI生成的试题失败", e);
            // 如果解析失败，创建一个包含AI原始回复的说明题
            ExamQuestion fallbackQuestion = new ExamQuestion();
            fallbackQuestion.setExamId(examId);
            fallbackQuestion.setQuestionType("SHORT_ANSWER");
            fallbackQuestion.setContent("AI生成的试题内容（原始数据）：\n" + aiResponse);
            fallbackQuestion.setScore(new java.math.BigDecimal("10.0"));
            fallbackQuestion.setDifficulty(getDifficultyLevel(difficulty));
            fallbackQuestion.setKnowledgePoint(knowledgePoint);
            fallbackQuestion.setQuestionOrder(1);
            fallbackQuestion.setAiGenerated(1);
            examQuestionRepository.save(fallbackQuestion);
        }
        
        return examQuestionRepository.findByExamIdOrderByQuestionOrder(examId);
    }
    
    /**
     * 将难度文本转换为数字等级
     */
    private Integer getDifficultyLevel(String difficulty) {
        if (difficulty == null) return 2;
        switch (difficulty.toLowerCase()) {
            case "简单":
            case "easy":
                return 1;
            case "困难":
            case "hard":
                return 3;
            default:
                return 2;
        }
    }
    
    /**
     * 添加试题
     */
    @Transactional
    public ExamQuestion addQuestion(ExamQuestion question) {
        return examQuestionRepository.save(question);
    }
    
    /**
     * 获取考试的所有试题
     */
    public List<ExamQuestion> getExamQuestions(Long examId) {
        return examQuestionRepository.findByExamIdOrderByQuestionOrder(examId);
    }
}


