package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.Result;
import org.example.backend.entity.*;
import org.example.backend.service.VideoInteractionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 视频互动控制器（模块三：弹幕、笔记、小测、进度）
 */
@RestController
@RequestMapping("/video-interaction")
@RequiredArgsConstructor
public class VideoInteractionController {
    
    private final VideoInteractionService interactionService;
    
    // ========== 播放进度 ==========
    
    /**
     * 更新播放进度
     */
    @PostMapping("/progress")
    public Result<VideoProgress> updateProgress(@RequestBody Map<String, Object> request) {
        Long videoId = Long.valueOf(request.get("videoId").toString());
        Long studentId = Long.valueOf(request.get("studentId").toString());
        Integer position = Integer.valueOf(request.get("position").toString());
        Integer duration = request.get("duration") != null ? Integer.valueOf(request.get("duration").toString()) : null;
        
        VideoProgress progress = interactionService.updateProgress(videoId, studentId, position, duration);
        return Result.success(progress);
    }
    
    /**
     * 获取播放进度
     */
    @GetMapping("/progress/{videoId}/{studentId}")
    public Result<VideoProgress> getProgress(@PathVariable Long videoId, @PathVariable Long studentId) {
        VideoProgress progress = interactionService.getProgress(videoId, studentId);
        return Result.success(progress);
    }
    
    /**
     * 获取学生的所有进度
     */
    @GetMapping("/progress/student/{studentId}")
    public Result<List<VideoProgress>> getStudentProgress(@PathVariable Long studentId) {
        List<VideoProgress> progressList = interactionService.getStudentProgress(studentId);
        return Result.success(progressList);
    }
    
    // ========== 视频笔记 ==========
    
    /**
     * 添加笔记
     */
    @PostMapping("/notes")
    public Result<VideoNote> addNote(@RequestBody Map<String, Object> request) {
        Long videoId = Long.valueOf(request.get("videoId").toString());
        Long studentId = Long.valueOf(request.get("studentId").toString());
        Integer timePoint = Integer.valueOf(request.get("timePoint").toString());
        String content = request.get("content").toString();
        
        VideoNote note = interactionService.addNote(videoId, studentId, timePoint, content);
        return Result.success(note);
    }
    
    /**
     * 更新笔记
     */
    @PutMapping("/notes/{id}")
    public Result<VideoNote> updateNote(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String content = request.get("content");
        VideoNote note = interactionService.updateNote(id, content);
        return Result.success(note);
    }
    
    /**
     * 删除笔记
     */
    @DeleteMapping("/notes/{id}")
    public Result<Void> deleteNote(@PathVariable Long id) {
        interactionService.deleteNote(id);
        return Result.success();
    }
    
    /**
     * 获取视频的笔记
     */
    @GetMapping("/notes/{videoId}/{studentId}")
    public Result<List<VideoNote>> getVideoNotes(@PathVariable Long videoId, @PathVariable Long studentId) {
        List<VideoNote> notes = interactionService.getVideoNotes(videoId, studentId);
        return Result.success(notes);
    }
    
    /**
     * 获取学生的所有笔记
     */
    @GetMapping("/notes/student/{studentId}")
    public Result<List<VideoNote>> getStudentNotes(@PathVariable Long studentId) {
        List<VideoNote> notes = interactionService.getStudentNotes(studentId);
        return Result.success(notes);
    }
    
    // ========== 视频弹幕 ==========
    
    /**
     * 发送弹幕
     */
    @PostMapping("/danmaku")
    public Result<VideoDanmaku> sendDanmaku(@RequestBody Map<String, Object> request) {
        Long videoId = Long.valueOf(request.get("videoId").toString());
        Long studentId = Long.valueOf(request.get("studentId").toString());
        Integer timePoint = Integer.valueOf(request.get("timePoint").toString());
        String content = request.get("content").toString();
        String color = (String) request.get("color");
        Integer type = request.get("type") != null ? Integer.valueOf(request.get("type").toString()) : 0;
        
        VideoDanmaku danmaku = interactionService.sendDanmaku(videoId, studentId, timePoint, content, color, type);
        return Result.success(danmaku);
    }
    
    /**
     * 获取视频弹幕
     */
    @GetMapping("/danmaku/{videoId}")
    public Result<List<VideoDanmaku>> getVideoDanmaku(@PathVariable Long videoId) {
        List<VideoDanmaku> danmakuList = interactionService.getVideoDanmaku(videoId);
        return Result.success(danmakuList);
    }
    
    /**
     * 隐藏弹幕
     */
    @PutMapping("/danmaku/{id}/hide")
    @PreAuthorize("hasAnyAuthority('TEACHER', 'ADMIN')")
    public Result<Void> hideDanmaku(@PathVariable Long id) {
        interactionService.hideDanmaku(id);
        return Result.success();
    }
    
    // ========== 视频小测验 ==========
    
    /**
     * 创建视频小测验
     */
    @PostMapping("/quiz")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<VideoQuiz> createQuiz(@RequestBody VideoQuiz quiz) {
        VideoQuiz created = interactionService.createQuiz(quiz);
        return Result.success(created);
    }
    
    /**
     * 获取视频的小测验
     */
    @GetMapping("/quiz/{videoId}")
    public Result<List<VideoQuiz>> getVideoQuizzes(@PathVariable Long videoId) {
        List<VideoQuiz> quizzes = interactionService.getVideoQuizzes(videoId);
        return Result.success(quizzes);
    }
    
    /**
     * 更新小测验
     */
    @PutMapping("/quiz/{id}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<VideoQuiz> updateQuiz(@PathVariable Long id, @RequestBody VideoQuiz quiz) {
        VideoQuiz updated = interactionService.updateQuiz(id, quiz);
        return Result.success(updated);
    }
    
    /**
     * 删除小测验
     */
    @DeleteMapping("/quiz/{id}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<Void> deleteQuiz(@PathVariable Long id) {
        interactionService.deleteQuiz(id);
        return Result.success();
    }
    
    // ========== 学习记录 ==========
    
    /**
     * 获取学习记录
     */
    @GetMapping("/learning-records/{studentId}")
    public Result<List<LearningRecord>> getLearningRecords(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long courseId) {
        List<LearningRecord> records = interactionService.getStudentLearningRecords(studentId, courseId);
        return Result.success(records);
    }
    
    // ========== 多语言字幕 ==========
    
    /**
     * 创建字幕
     */
    @PostMapping("/subtitles")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<VideoSubtitle> createSubtitle(@RequestBody VideoSubtitle subtitle) {
        VideoSubtitle created = interactionService.createSubtitle(subtitle);
        return Result.success(created);
    }
    
    /**
     * AI翻译字幕
     */
    @PostMapping("/subtitles/translate")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<VideoSubtitle> translateSubtitle(@RequestBody Map<String, Object> request) {
        Long videoId = Long.valueOf(request.get("videoId").toString());
        String sourceLanguage = request.get("sourceLanguage").toString();
        String targetLanguage = request.get("targetLanguage").toString();
        String originalContent = request.get("originalContent").toString();
        
        VideoSubtitle subtitle = interactionService.translateSubtitle(videoId, sourceLanguage, targetLanguage, originalContent);
        return Result.success(subtitle);
    }
    
    /**
     * 获取视频的所有字幕
     */
    @GetMapping("/subtitles/video/{videoId}")
    public Result<List<VideoSubtitle>> getVideoSubtitles(@PathVariable Long videoId) {
        List<VideoSubtitle> subtitles = interactionService.getVideoSubtitles(videoId);
        return Result.success(subtitles);
    }
    
    /**
     * 获取指定语言的字幕
     */
    @GetMapping("/subtitles/{videoId}/{language}")
    public Result<VideoSubtitle> getSubtitleByLanguage(@PathVariable Long videoId, @PathVariable String language) {
        VideoSubtitle subtitle = interactionService.getSubtitleByLanguage(videoId, language);
        return Result.success(subtitle);
    }
    
    /**
     * 删除字幕
     */
    @DeleteMapping("/subtitles/{id}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<Void> deleteSubtitle(@PathVariable Long id) {
        interactionService.deleteSubtitle(id);
        return Result.success();
    }
    
    // ========== 知识点弹窗 ==========
    
    /**
     * 创建知识点
     */
    @PostMapping("/knowledge-points")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<KnowledgePoint> createKnowledgePoint(@RequestBody KnowledgePoint point) {
        KnowledgePoint created = interactionService.createKnowledgePoint(point);
        return Result.success(created);
    }
    
    /**
     * 获取视频的知识点
     */
    @GetMapping("/knowledge-points/{videoId}")
    public Result<List<KnowledgePoint>> getVideoKnowledgePoints(@PathVariable Long videoId) {
        List<KnowledgePoint> points = interactionService.getVideoKnowledgePoints(videoId);
        return Result.success(points);
    }
    
    /**
     * 更新知识点
     */
    @PutMapping("/knowledge-points/{id}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<KnowledgePoint> updateKnowledgePoint(@PathVariable Long id, @RequestBody KnowledgePoint point) {
        KnowledgePoint updated = interactionService.updateKnowledgePoint(id, point);
        return Result.success(updated);
    }
    
    /**
     * 删除知识点
     */
    @DeleteMapping("/knowledge-points/{id}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<Void> deleteKnowledgePoint(@PathVariable Long id) {
        interactionService.deleteKnowledgePoint(id);
        return Result.success();
    }
    
    // ========== 笔记AI整理 ==========
    
    /**
     * AI自动整理笔记
     */
    @PostMapping("/notes/organize")
    public Result<String> organizeNotes(@RequestBody Map<String, Object> request) {
        Long studentId = Long.valueOf(request.get("studentId").toString());
        Long videoId = Long.valueOf(request.get("videoId").toString());
        
        String organizedNotes = interactionService.organizeNotesWithAI(studentId, videoId);
        return Result.success(organizedNotes);
    }
}

