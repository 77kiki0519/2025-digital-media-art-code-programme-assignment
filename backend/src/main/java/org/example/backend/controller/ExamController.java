package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.Result;
import org.example.backend.entity.Exam;
import org.example.backend.entity.ExamQuestion;
import org.example.backend.entity.ExamSubmission;
import org.example.backend.repository.ExamSubmissionRepository;
import org.example.backend.repository.UserRepository;
import org.example.backend.service.ExamService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 考试控制器
 */
@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamController {
    
    private final ExamService examService;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final UserRepository userRepository;
    
    /**
     * 创建考试
     */
    @PostMapping
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<Exam> createExam(@RequestBody Exam exam) {
        Exam created = examService.createExam(exam);
        return Result.success(created);
    }
    
    /**
     * 更新考试
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<Exam> updateExam(@PathVariable Long id, @RequestBody Exam exam) {
        Exam updated = examService.updateExam(id, exam);
        return Result.success(updated);
    }
    
    /**
     * 删除考试
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<Void> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return Result.success();
    }
    
    /**
     * 获取考试详情
     */
    @GetMapping("/{id}")
    public Result<Exam> getExam(@PathVariable Long id) {
        Exam exam = examService.getExamById(id);
        return Result.success(exam);
    }
    
    /**
     * 获取课程的所有考试
     */
    @GetMapping("/course/{courseId}")
    public Result<List<Exam>> getCourseExams(@PathVariable Long courseId) {
        List<Exam> exams = examService.getCourseExams(courseId);
        return Result.success(exams);
    }
    
    /**
     * AI自动生成试题
     */
    @PostMapping("/{examId}/generate-questions")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<List<ExamQuestion>> generateQuestions(
            @PathVariable Long examId,
            @RequestParam String knowledgePoint,
            @RequestParam String difficulty,
            @RequestParam Integer count) {
        List<ExamQuestion> questions = examService.generateQuestions(examId, knowledgePoint, difficulty, count);
        return Result.success(questions);
    }
    
    /**
     * 添加试题
     */
    @PostMapping("/questions")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<ExamQuestion> addQuestion(@RequestBody ExamQuestion question) {
        ExamQuestion created = examService.addQuestion(question);
        return Result.success(created);
    }
    
    /**
     * 获取考试试题
     */
    @GetMapping("/{examId}/questions")
    public Result<List<ExamQuestion>> getExamQuestions(@PathVariable Long examId) {
        List<ExamQuestion> questions = examService.getExamQuestions(examId);
        return Result.success(questions);
    }
    
    /**
     * 获取考试的所有提交记录（教师）
     */
    @GetMapping("/{examId}/submissions")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<List<Map<String, Object>>> getExamSubmissions(@PathVariable Long examId) {
        List<ExamSubmission> submissions = examSubmissionRepository.findByExamId(examId);
        List<Map<String, Object>> result = submissions.stream().map(submission -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", submission.getId());
            map.put("examId", submission.getExamId());
            map.put("studentId", submission.getStudentId());
            map.put("startTime", submission.getStartTime());
            map.put("submitTime", submission.getSubmitTime());
            map.put("duration", submission.getDuration());
            map.put("totalScore", submission.getTotalScore());
            map.put("status", submission.getStatus());
            map.put("aiScored", submission.getAiScored());
            // 添加学生信息
            userRepository.findById(submission.getStudentId()).ifPresent(user -> {
                map.put("studentName", user.getRealName());
                map.put("studentNo", user.getStudentNo());
            });
            return map;
        }).collect(Collectors.toList());
        return Result.success(result);
    }
    
    /**
     * 提交考试（学生）- 只能提交一次
     */
    @PostMapping("/{examId}/submit")
    public Result<ExamSubmission> submitExam(
            @PathVariable Long examId,
            @RequestBody ExamSubmission submission) {
        submission.setExamId(examId);
        
        // 检查是否已有提交记录
        ExamSubmission existing = examSubmissionRepository
                .findByExamIdAndStudentId(examId, submission.getStudentId())
                .orElse(null);
        
        if (existing != null) {
            // 已提交，不允许重复提交
            return Result.error("您已经参加过该考试，不能重复提交");
        }
        
        // 新建提交记录
        submission.setSubmitTime(java.time.LocalDateTime.now());
        ExamSubmission saved = examSubmissionRepository.save(submission);
        return Result.success(saved);
    }
    
    /**
     * 检查学生是否已提交考试
     */
    @GetMapping("/{examId}/check-submission/{studentId}")
    public Result<Boolean> checkSubmission(
            @PathVariable Long examId,
            @PathVariable Long studentId) {
        ExamSubmission existing = examSubmissionRepository
                .findByExamIdAndStudentId(examId, studentId)
                .orElse(null);
        return Result.success(existing != null);
    }
}


