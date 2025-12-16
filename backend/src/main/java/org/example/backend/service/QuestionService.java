package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.PageResult;
import org.example.backend.entity.Answer;
import org.example.backend.entity.Question;
import org.example.backend.exception.BusinessException;
import org.example.backend.repository.AnswerRepository;
import org.example.backend.repository.QuestionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 提问服务
 */
@Service
@RequiredArgsConstructor
public class QuestionService {
    
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final DeepSeekService deepSeekService;
    
    /**
     * 创建提问
     */
    @Transactional
    public Question createQuestion(Question question) {
        return questionRepository.save(question);
    }
    
    /**
     * AI回答问题并保存
     */
    @Transactional
    public String answerWithAI(Long questionId, String courseContext) {
        Question question = getQuestionById(questionId);
        String answerContent = deepSeekService.answerQuestion(question.getContent(), courseContext);
        
        // 保存AI回答
        Answer answer = new Answer();
        answer.setQuestionId(questionId);
        answer.setAnswererType("AI");
        answer.setContent(answerContent);
        answerRepository.save(answer);
        
        // 更新问题状态为已回答
        question.setStatus(1);
        questionRepository.save(question);
        
        return answerContent;
    }
    
    /**
     * 保存AI回答（流式完成后调用）
     */
    @Transactional
    public void saveAIAnswer(Long questionId, String answerContent) {
        Answer answer = new Answer();
        answer.setQuestionId(questionId);
        answer.setAnswererType("AI");
        answer.setContent(answerContent);
        answerRepository.save(answer);
        
        // 更新问题状态为已回答
        Question question = getQuestionById(questionId);
        question.setStatus(1);
        questionRepository.save(question);
    }
    
    /**
     * 获取问题的回答
     */
    public List<Answer> getQuestionAnswers(Long questionId) {
        return answerRepository.findByQuestionIdOrderByCreatedAtAsc(questionId);
    }
    
    /**
     * 获取聊天历史（问题+回答）
     */
    public List<Map<String, Object>> getChatHistory(Long studentId, Long courseId, Integer limit) {
        Pageable pageable = PageRequest.of(0, limit != null ? limit : 50, Sort.by("createdAt").ascending());
        Page<Question> questionPage;
        
        if (courseId != null) {
            questionPage = questionRepository.findByStudentIdAndCourseId(studentId, courseId, pageable);
        } else {
            questionPage = questionRepository.findByStudentId(studentId, pageable);
        }
        
        List<Map<String, Object>> history = new java.util.ArrayList<>();
        
        for (Question question : questionPage.getContent()) {
            // 添加问题
            Map<String, Object> questionMap = new HashMap<>();
            questionMap.put("role", "user");
            questionMap.put("content", question.getContent());
            questionMap.put("time", question.getCreatedAt());
            questionMap.put("questionId", question.getId());
            history.add(questionMap);
            
            // 获取并添加回答
            List<Answer> answers = answerRepository.findByQuestionIdOrderByCreatedAtAsc(question.getId());
            for (Answer answer : answers) {
                Map<String, Object> answerMap = new HashMap<>();
                answerMap.put("role", "assistant");
                answerMap.put("content", answer.getContent());
                answerMap.put("time", answer.getCreatedAt());
                answerMap.put("answererType", answer.getAnswererType());
                history.add(answerMap);
            }
        }
        
        return history;
    }
    
    /**
     * 获取问题详情
     */
    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("问题不存在"));
    }
    
    /**
     * 获取课程的问题列表
     */
    public PageResult<Question> getCourseQuestions(Long courseId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Question> questionPage = questionRepository.findByCourseId(courseId, pageable);
        
        return new PageResult<>(
                questionPage.getContent(),
                questionPage.getTotalElements(),
                (long) page,
                (long) size
        );
    }
    
    /**
     * 获取学生的问题列表
     */
    public PageResult<Question> getStudentQuestions(Long studentId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Question> questionPage = questionRepository.findByStudentId(studentId, pageable);
        
        return new PageResult<>(
                questionPage.getContent(),
                questionPage.getTotalElements(),
                (long) page,
                (long) size
        );
    }
    
    /**
     * 更新问题状态
     */
    @Transactional
    public Question updateQuestionStatus(Long id, Integer status) {
        Question question = getQuestionById(id);
        question.setStatus(status);
        return questionRepository.save(question);
    }
}


