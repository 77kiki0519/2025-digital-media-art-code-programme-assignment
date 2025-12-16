package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.ChapterExercise;
import org.example.backend.entity.ChapterExerciseQuestion;
import org.example.backend.entity.ChapterExerciseSubmission;
import org.example.backend.exception.BusinessException;
import org.example.backend.repository.ChapterExerciseQuestionRepository;
import org.example.backend.repository.ChapterExerciseRepository;
import org.example.backend.repository.ChapterExerciseSubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 章节练习服务（模块三：线上课程）
 */
@Service
@RequiredArgsConstructor
public class ChapterExerciseService {
    
    private final ChapterExerciseRepository exerciseRepository;
    private final ChapterExerciseQuestionRepository questionRepository;
    private final ChapterExerciseSubmissionRepository submissionRepository;
    
    // ========== 章节练习管理 ==========
    
    /**
     * 创建章节练习
     */
    @Transactional
    public ChapterExercise createExercise(ChapterExercise exercise) {
        return exerciseRepository.save(exercise);
    }
    
    /**
     * 更新章节练习
     */
    @Transactional
    public ChapterExercise updateExercise(Long id, ChapterExercise exercise) {
        ChapterExercise existing = getExerciseById(id);
        existing.setExerciseName(exercise.getExerciseName());
        existing.setDescription(exercise.getDescription());
        existing.setTotalScore(exercise.getTotalScore());
        existing.setPassScore(exercise.getPassScore());
        existing.setTimeLimit(exercise.getTimeLimit());
        existing.setAllowRetry(exercise.getAllowRetry());
        existing.setStatus(exercise.getStatus());
        return exerciseRepository.save(existing);
    }
    
    /**
     * 删除章节练习
     */
    @Transactional
    public void deleteExercise(Long id) {
        exerciseRepository.deleteById(id);
    }
    
    /**
     * 获取章节练习详情
     */
    public ChapterExercise getExerciseById(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("章节练习不存在"));
    }
    
    /**
     * 获取章节的所有练习
     */
    public List<ChapterExercise> getExercisesByChapterId(Long chapterId) {
        return exerciseRepository.findByChapterId(chapterId);
    }
    
    // ========== 练习题目管理 ==========
    
    /**
     * 添加练习题目
     */
    @Transactional
    public ChapterExerciseQuestion addQuestion(ChapterExerciseQuestion question) {
        return questionRepository.save(question);
    }
    
    /**
     * 更新练习题目
     */
    @Transactional
    public ChapterExerciseQuestion updateQuestion(Long id, ChapterExerciseQuestion question) {
        ChapterExerciseQuestion existing = questionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("题目不存在"));
        existing.setQuestionType(question.getQuestionType());
        existing.setContent(question.getContent());
        existing.setOptions(question.getOptions());
        existing.setCorrectAnswer(question.getCorrectAnswer());
        existing.setScore(question.getScore());
        existing.setAnalysis(question.getAnalysis());
        existing.setQuestionOrder(question.getQuestionOrder());
        return questionRepository.save(existing);
    }
    
    /**
     * 删除练习题目
     */
    @Transactional
    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }
    
    /**
     * 获取练习的所有题目
     */
    public List<ChapterExerciseQuestion> getQuestionsByExerciseId(Long exerciseId) {
        return questionRepository.findByExerciseIdOrderByQuestionOrder(exerciseId);
    }
    
    // ========== 练习提交与批改 ==========
    
    /**
     * 提交练习
     */
    @Transactional
    public ChapterExerciseSubmission submitExercise(Long exerciseId, Long studentId, String answers, Integer duration) {
        ChapterExercise exercise = getExerciseById(exerciseId);
        
        // 检查是否允许重做
        List<ChapterExerciseSubmission> previousSubmissions = submissionRepository
                .findByExerciseIdAndStudentId(exerciseId, studentId);
        
        if (!previousSubmissions.isEmpty() && exercise.getAllowRetry() == 0) {
            throw new BusinessException("该练习不允许重做");
        }
        
        // 自动批改
        List<ChapterExerciseQuestion> questions = getQuestionsByExerciseId(exerciseId);
        BigDecimal totalScore = autoGrade(questions, answers);
        
        // 创建提交记录
        ChapterExerciseSubmission submission = new ChapterExerciseSubmission();
        submission.setExerciseId(exerciseId);
        submission.setStudentId(studentId);
        submission.setAnswers(answers);
        submission.setScore(totalScore);
        submission.setIsPassed(totalScore.compareTo(BigDecimal.valueOf(exercise.getPassScore())) >= 0 ? 1 : 0);
        submission.setDuration(duration);
        submission.setRetryCount(previousSubmissions.size());
        submission.setSubmitTime(LocalDateTime.now());
        
        return submissionRepository.save(submission);
    }
    
    /**
     * 自动批改
     */
    private BigDecimal autoGrade(List<ChapterExerciseQuestion> questions, String answersJson) {
        BigDecimal totalScore = BigDecimal.ZERO;
        
        // TODO: 解析学生答案JSON，与正确答案对比
        // 这里简化处理，实际需要解析JSON并逐题对比
        // {
        //   "1": "A",
        //   "2": "B,C",
        //   "3": "这是简答题答案"
        // }
        
        try {
            // 简单实现：随机给分（实际应该精确比对）
            for (ChapterExerciseQuestion question : questions) {
                // 实际应该从answersJson中取出对应题目的答案进行比对
                // 这里暂时给60-100分之间的随机分数
                BigDecimal questionScore = question.getScore();
                BigDecimal earnedScore = questionScore.multiply(BigDecimal.valueOf(0.8)); // 假设答对80%
                totalScore = totalScore.add(earnedScore);
            }
        } catch (Exception e) {
            throw new BusinessException("答案格式错误");
        }
        
        return totalScore;
    }
    
    /**
     * 获取学生的提交记录
     */
    public List<ChapterExerciseSubmission> getStudentSubmissions(Long studentId, Long exerciseId) {
        if (exerciseId != null) {
            return submissionRepository.findByExerciseIdAndStudentId(exerciseId, studentId);
        }
        return submissionRepository.findByStudentId(studentId);
    }
    
    /**
     * 获取练习的所有提交
     */
    public List<ChapterExerciseSubmission> getExerciseSubmissions(Long exerciseId) {
        return submissionRepository.findByExerciseId(exerciseId);
    }
    
    /**
     * 获取提交详情
     */
    public ChapterExerciseSubmission getSubmissionById(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("提交记录不存在"));
    }
}


