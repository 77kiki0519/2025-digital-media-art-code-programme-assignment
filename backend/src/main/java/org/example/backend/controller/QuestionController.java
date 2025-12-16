package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.PageResult;
import org.example.backend.common.Result;
import org.example.backend.entity.Question;
import org.example.backend.service.QuestionService;
import org.example.backend.service.DeepSeekService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import java.util.List;
import java.util.Map;

/**
 * 提问控制器
 */
@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {
    
    private final QuestionService questionService;
    private final DeepSeekService deepSeekService;
    
    /**
     * 创建提问
     */
    @PostMapping
    public Result<Question> createQuestion(@RequestBody Question question) {
        Question created = questionService.createQuestion(question);
        return Result.success(created);
    }
    
    /**
     * AI回答问题（非流式）
     */
    @PostMapping("/{id}/answer-ai")
    public Result<String> answerWithAI(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        String courseContext = request != null ? request.get("courseContext") : null;
        String answer = questionService.answerWithAI(id, courseContext);
        return Result.success(answer);
    }
    
    /**
     * AI回答问题（流式输出）
     */
    @GetMapping(value = "/{id}/answer-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> answerWithAIStream(
            @PathVariable Long id,
            @RequestParam(required = false) String courseContext) {
        Question question = questionService.getQuestionById(id);
        if (question == null) {
            return Flux.just("问题不存在");
        }
        return deepSeekService.answerQuestionStream(question.getContent(), courseContext)
                .map(content -> "data: " + content.replace("\n", "\\n") + "\n\n");
    }
    
    /**
     * 直接流式问答（不保存问题）
     */
    @PostMapping(value = "/chat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        String courseContext = request.get("courseContext");
        return deepSeekService.answerQuestionStream(question, courseContext)
                .map(content -> "data: " + content.replace("\n", "\\n") + "\n\n");
    }
    
    /**
     * 获取问题详情
     */
    @GetMapping("/{id}")
    public Result<Question> getQuestion(@PathVariable Long id) {
        Question question = questionService.getQuestionById(id);
        return Result.success(question);
    }
    
    /**
     * 获取课程的问题列表
     */
    @GetMapping("/course/{courseId}")
    public Result<PageResult<Question>> getCourseQuestions(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<Question> result = questionService.getCourseQuestions(courseId, page, size);
        return Result.success(result);
    }
    
    /**
     * 获取学生的问题列表
     */
    @GetMapping("/student/{studentId}")
    public Result<PageResult<Question>> getStudentQuestions(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<Question> result = questionService.getStudentQuestions(studentId, page, size);
        return Result.success(result);
    }
    
    /**
     * 更新问题状态
     */
    @PutMapping("/{id}/status")
    public Result<Question> updateQuestionStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        Question question = questionService.updateQuestionStatus(id, status);
        return Result.success(question);
    }
    
    /**
     * 获取聊天历史
     */
    @GetMapping("/history/{studentId}")
    public Result<List<Map<String, Object>>> getChatHistory(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "50") Integer limit) {
        List<Map<String, Object>> history = questionService.getChatHistory(studentId, courseId, limit);
        return Result.success(history);
    }
    
    /**
     * 保存AI回答（流式完成后调用）
     */
    @PostMapping("/{id}/save-answer")
    public Result<Void> saveAnswer(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String content = request.get("content");
        questionService.saveAIAnswer(id, content);
        return Result.success();
    }
}

