package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.Result;
import org.example.backend.entity.ChapterExercise;
import org.example.backend.entity.ChapterExerciseQuestion;
import org.example.backend.entity.ChapterExerciseSubmission;
import org.example.backend.service.ChapterExerciseService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 章节练习控制器（模块三：线上课程）
 */
@RestController
@RequestMapping("/chapter-exercises")
@RequiredArgsConstructor
public class ChapterExerciseController {
    
    private final ChapterExerciseService exerciseService;
    
    // ========== 练习管理 ==========
    
    /**
     * 创建章节练习
     */
    @PostMapping
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<ChapterExercise> createExercise(@RequestBody ChapterExercise exercise) {
        ChapterExercise created = exerciseService.createExercise(exercise);
        return Result.success(created);
    }
    
    /**
     * 更新章节练习
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<ChapterExercise> updateExercise(@PathVariable Long id, @RequestBody ChapterExercise exercise) {
        ChapterExercise updated = exerciseService.updateExercise(id, exercise);
        return Result.success(updated);
    }
    
    /**
     * 删除章节练习
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<Void> deleteExercise(@PathVariable Long id) {
        exerciseService.deleteExercise(id);
        return Result.success();
    }
    
    /**
     * 获取练习详情
     */
    @GetMapping("/{id}")
    public Result<ChapterExercise> getExercise(@PathVariable Long id) {
        ChapterExercise exercise = exerciseService.getExerciseById(id);
        return Result.success(exercise);
    }
    
    /**
     * 获取章节的所有练习
     */
    @GetMapping("/chapter/{chapterId}")
    public Result<List<ChapterExercise>> getExercisesByChapter(@PathVariable Long chapterId) {
        List<ChapterExercise> exercises = exerciseService.getExercisesByChapterId(chapterId);
        return Result.success(exercises);
    }
    
    // ========== 题目管理 ==========
    
    /**
     * 添加练习题目
     */
    @PostMapping("/questions")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<ChapterExerciseQuestion> addQuestion(@RequestBody ChapterExerciseQuestion question) {
        ChapterExerciseQuestion created = exerciseService.addQuestion(question);
        return Result.success(created);
    }
    
    /**
     * 更新练习题目
     */
    @PutMapping("/questions/{id}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<ChapterExerciseQuestion> updateQuestion(@PathVariable Long id, 
                                                          @RequestBody ChapterExerciseQuestion question) {
        ChapterExerciseQuestion updated = exerciseService.updateQuestion(id, question);
        return Result.success(updated);
    }
    
    /**
     * 删除练习题目
     */
    @DeleteMapping("/questions/{id}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        exerciseService.deleteQuestion(id);
        return Result.success();
    }
    
    /**
     * 获取练习的所有题目
     */
    @GetMapping("/{exerciseId}/questions")
    public Result<List<ChapterExerciseQuestion>> getQuestions(@PathVariable Long exerciseId) {
        List<ChapterExerciseQuestion> questions = exerciseService.getQuestionsByExerciseId(exerciseId);
        return Result.success(questions);
    }
    
    // ========== 提交与批改 ==========
    
    /**
     * 提交练习
     */
    @PostMapping("/submit")
    public Result<ChapterExerciseSubmission> submitExercise(@RequestBody Map<String, Object> request) {
        Long exerciseId = Long.valueOf(request.get("exerciseId").toString());
        Long studentId = Long.valueOf(request.get("studentId").toString());
        String answers = request.get("answers").toString();
        Integer duration = request.get("duration") != null ? 
                Integer.valueOf(request.get("duration").toString()) : null;
        
        ChapterExerciseSubmission submission = exerciseService.submitExercise(
                exerciseId, studentId, answers, duration);
        return Result.success(submission);
    }
    
    /**
     * 获取学生的提交记录
     */
    @GetMapping("/submissions/student/{studentId}")
    public Result<List<ChapterExerciseSubmission>> getStudentSubmissions(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long exerciseId) {
        List<ChapterExerciseSubmission> submissions = exerciseService.getStudentSubmissions(studentId, exerciseId);
        return Result.success(submissions);
    }
    
    /**
     * 获取练习的所有提交
     */
    @GetMapping("/{exerciseId}/submissions")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<List<ChapterExerciseSubmission>> getExerciseSubmissions(@PathVariable Long exerciseId) {
        List<ChapterExerciseSubmission> submissions = exerciseService.getExerciseSubmissions(exerciseId);
        return Result.success(submissions);
    }
    
    /**
     * 获取提交详情
     */
    @GetMapping("/submissions/{id}")
    public Result<ChapterExerciseSubmission> getSubmission(@PathVariable Long id) {
        ChapterExerciseSubmission submission = exerciseService.getSubmissionById(id);
        return Result.success(submission);
    }
}


