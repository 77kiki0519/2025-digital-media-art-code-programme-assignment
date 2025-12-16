package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.*;
import org.example.backend.exception.BusinessException;
import org.example.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 视频互动服务（模块三：弹幕、笔记、小测、字幕、知识点）
 */
@Service
@RequiredArgsConstructor
public class VideoInteractionService {
    
    private final VideoProgressRepository progressRepository;
    private final VideoNoteRepository noteRepository;
    private final VideoDanmakuRepository danmakuRepository;
    private final VideoQuizRepository quizRepository;
    private final LearningRecordRepository learningRecordRepository;
    private final VideoSubtitleRepository subtitleRepository;
    private final KnowledgePointRepository knowledgePointRepository;
    private final DeepSeekService deepSeekService;
    
    // ========== 视频播放进度 ==========
    
    /**
     * 更新播放进度
     */
    @Transactional
    public VideoProgress updateProgress(Long videoId, Long studentId, Integer position, Integer duration) {
        VideoProgress progress = progressRepository.findByVideoIdAndStudentId(videoId, studentId)
                .orElse(new VideoProgress());
        
        progress.setVideoId(videoId);
        progress.setStudentId(studentId);
        progress.setProgress(position);
        progress.setLastPosition(position);
        progress.setDuration(duration);
        progress.setLastPlayTime(LocalDateTime.now());
        
        if (progress.getPlayTimes() == null) {
            progress.setPlayTimes(1);
        } else {
            progress.setPlayTimes(progress.getPlayTimes() + 1);
        }
        
        // 判断是否完成（观看进度超过90%）
        if (duration != null && position != null && position >= duration * 0.9) {
            progress.setCompleted(1);
        }
        
        progress = progressRepository.save(progress);
        
        // 记录学习行为
        recordLearningAction(studentId, null, null, videoId, "VIEW", position - (progress.getLastPosition() != null ? progress.getLastPosition() : 0));
        
        return progress;
    }
    
    /**
     * 获取学生的播放进度
     */
    public VideoProgress getProgress(Long videoId, Long studentId) {
        return progressRepository.findByVideoIdAndStudentId(videoId, studentId)
                .orElse(null);
    }
    
    /**
     * 获取学生的所有进度
     */
    public List<VideoProgress> getStudentProgress(Long studentId) {
        return progressRepository.findByStudentId(studentId);
    }
    
    // ========== 视频笔记 ==========
    
    /**
     * 添加笔记
     */
    @Transactional
    public VideoNote addNote(Long videoId, Long studentId, Integer timePoint, String content) {
        VideoNote note = new VideoNote();
        note.setVideoId(videoId);
        note.setStudentId(studentId);
        note.setTimePoint(timePoint);
        note.setContent(content);
        
        note = noteRepository.save(note);
        
        // 记录学习行为
        recordLearningAction(studentId, null, null, videoId, "NOTE", 0);
        
        return note;
    }
    
    /**
     * 更新笔记
     */
    @Transactional
    public VideoNote updateNote(Long noteId, String content) {
        VideoNote note = noteRepository.findById(noteId)
                .orElseThrow(() -> new BusinessException("笔记不存在"));
        note.setContent(content);
        return noteRepository.save(note);
    }
    
    /**
     * 删除笔记
     */
    @Transactional
    public void deleteNote(Long noteId) {
        noteRepository.deleteById(noteId);
    }
    
    /**
     * 获取视频的笔记
     */
    public List<VideoNote> getVideoNotes(Long videoId, Long studentId) {
        return noteRepository.findByVideoIdAndStudentId(videoId, studentId);
    }
    
    /**
     * 获取学生的所有笔记
     */
    public List<VideoNote> getStudentNotes(Long studentId) {
        return noteRepository.findByStudentId(studentId);
    }
    
    // ========== 视频弹幕 ==========
    
    /**
     * 发送弹幕
     */
    @Transactional
    public VideoDanmaku sendDanmaku(Long videoId, Long studentId, Integer timePoint, String content, String color, Integer type) {
        VideoDanmaku danmaku = new VideoDanmaku();
        danmaku.setVideoId(videoId);
        danmaku.setStudentId(studentId);
        danmaku.setTimePoint(timePoint);
        danmaku.setContent(content);
        danmaku.setColor(color != null ? color : "#FFFFFF");
        danmaku.setType(type != null ? type : 0);
        danmaku.setStatus(1);
        
        return danmakuRepository.save(danmaku);
    }
    
    /**
     * 获取视频弹幕
     */
    public List<VideoDanmaku> getVideoDanmaku(Long videoId) {
        return danmakuRepository.findByVideoIdAndStatus(videoId, 1);
    }
    
    /**
     * 隐藏弹幕（管理员功能）
     */
    @Transactional
    public void hideDanmaku(Long danmakuId) {
        VideoDanmaku danmaku = danmakuRepository.findById(danmakuId)
                .orElseThrow(() -> new BusinessException("弹幕不存在"));
        danmaku.setStatus(0);
        danmakuRepository.save(danmaku);
    }
    
    // ========== 视频小测验 ==========
    
    /**
     * 创建视频小测验
     */
    @Transactional
    public VideoQuiz createQuiz(VideoQuiz quiz) {
        return quizRepository.save(quiz);
    }
    
    /**
     * 获取视频的小测验
     */
    public List<VideoQuiz> getVideoQuizzes(Long videoId) {
        return quizRepository.findByVideoIdAndEnabled(videoId, 1);
    }
    
    /**
     * 更新小测验
     */
    @Transactional
    public VideoQuiz updateQuiz(Long quizId, VideoQuiz quiz) {
        VideoQuiz existing = quizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException("测验不存在"));
        existing.setTimePoint(quiz.getTimePoint());
        existing.setQuestionType(quiz.getQuestionType());
        existing.setContent(quiz.getContent());
        existing.setOptions(quiz.getOptions());
        existing.setCorrectAnswer(quiz.getCorrectAnswer());
        existing.setAnalysis(quiz.getAnalysis());
        return quizRepository.save(existing);
    }
    
    /**
     * 删除小测验
     */
    @Transactional
    public void deleteQuiz(Long quizId) {
        quizRepository.deleteById(quizId);
    }
    
    // ========== 学习记录 ==========
    
    /**
     * 记录学习行为
     */
    @Transactional
    public void recordLearningAction(Long studentId, Long courseId, Long chapterId, Long videoId, 
                                     String actionType, Integer duration) {
        LearningRecord record = new LearningRecord();
        record.setStudentId(studentId);
        record.setCourseId(courseId);
        record.setChapterId(chapterId);
        record.setVideoId(videoId);
        record.setActionType(actionType);
        record.setActionTime(LocalDateTime.now());
        record.setDuration(duration);
        
        learningRecordRepository.save(record);
    }
    
    /**
     * 获取学生的学习记录
     */
    public List<LearningRecord> getStudentLearningRecords(Long studentId, Long courseId) {
        if (courseId != null) {
            return learningRecordRepository.findByStudentIdAndCourseId(studentId, courseId);
        }
        return learningRecordRepository.findByStudentId(studentId);
    }
    
    // ========== 多语言字幕（AI翻译） ==========
    
    /**
     * 创建字幕
     */
    @Transactional
    public VideoSubtitle createSubtitle(VideoSubtitle subtitle) {
        return subtitleRepository.save(subtitle);
    }
    
    /**
     * AI翻译字幕
     */
    @Transactional
    public VideoSubtitle translateSubtitle(Long videoId, String sourceLanguage, String targetLanguage, String originalContent) {
        // 调用AI翻译服务
        String prompt = String.format(
            "请将以下%s字幕翻译成%s，保持时间轴格式不变：\n\n%s",
            getLanguageName(sourceLanguage),
            getLanguageName(targetLanguage),
            originalContent
        );
        
        String translatedContent = deepSeekService.chat(prompt);
        
        // 保存翻译后的字幕
        VideoSubtitle subtitle = new VideoSubtitle();
        subtitle.setVideoId(videoId);
        subtitle.setLanguage(targetLanguage);
        subtitle.setLanguageName(getLanguageName(targetLanguage));
        subtitle.setSubtitleContent(translatedContent);
        subtitle.setAiTranslated(1);
        subtitle.setIsDefault(0);
        
        return subtitleRepository.save(subtitle);
    }
    
    /**
     * 获取视频的所有字幕
     */
    public List<VideoSubtitle> getVideoSubtitles(Long videoId) {
        return subtitleRepository.findByVideoId(videoId);
    }
    
    /**
     * 获取指定语言的字幕
     */
    public VideoSubtitle getSubtitleByLanguage(Long videoId, String language) {
        return subtitleRepository.findByVideoIdAndLanguage(videoId, language).orElse(null);
    }
    
    /**
     * 删除字幕
     */
    @Transactional
    public void deleteSubtitle(Long subtitleId) {
        subtitleRepository.deleteById(subtitleId);
    }
    
    // ========== 知识点弹窗 ==========
    
    /**
     * 创建知识点
     */
    @Transactional
    public KnowledgePoint createKnowledgePoint(KnowledgePoint point) {
        return knowledgePointRepository.save(point);
    }
    
    /**
     * 获取视频的知识点
     */
    public List<KnowledgePoint> getVideoKnowledgePoints(Long videoId) {
        return knowledgePointRepository.findByVideoIdAndEnabled(videoId, 1);
    }
    
    /**
     * 更新知识点
     */
    @Transactional
    public KnowledgePoint updateKnowledgePoint(Long id, KnowledgePoint point) {
        KnowledgePoint existing = knowledgePointRepository.findById(id)
                .orElseThrow(() -> new BusinessException("知识点不存在"));
        existing.setTimePoint(point.getTimePoint());
        existing.setTitle(point.getTitle());
        existing.setContent(point.getContent());
        existing.setPointType(point.getPointType());
        existing.setRelatedUrl(point.getRelatedUrl());
        return knowledgePointRepository.save(existing);
    }
    
    /**
     * 删除知识点
     */
    @Transactional
    public void deleteKnowledgePoint(Long id) {
        knowledgePointRepository.deleteById(id);
    }
    
    // ========== 笔记AI整理 ==========
    
    /**
     * AI自动整理笔记
     */
    public String organizeNotesWithAI(Long studentId, Long videoId) {
        // 获取学生的所有笔记
        List<VideoNote> notes = noteRepository.findByVideoIdAndStudentId(videoId, studentId);
        
        if (notes.isEmpty()) {
            throw new BusinessException("暂无笔记可整理");
        }
        
        // 构建笔记内容
        StringBuilder notesContent = new StringBuilder();
        for (VideoNote note : notes) {
            notesContent.append(String.format("[%s] %s\n", 
                formatTime(note.getTimePoint()), note.getContent()));
        }
        
        // 调用AI整理
        String prompt = "请将以下学习笔记进行智能整理，提取关键知识点，分类归纳，生成结构化的学习总结：\n\n" + 
                       notesContent.toString();
        
        return deepSeekService.chat(prompt);
    }
    
    /**
     * 格式化时间
     */
    private String formatTime(int seconds) {
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;
        if (h > 0) {
            return String.format("%02d:%02d:%02d", h, m, s);
        }
        return String.format("%02d:%02d", m, s);
    }
    
    /**
     * 获取语言名称
     */
    private String getLanguageName(String languageCode) {
        switch (languageCode) {
            case "zh-CN": return "中文";
            case "en-US": return "英语";
            case "ja-JP": return "日语";
            case "ko-KR": return "韩语";
            case "fr-FR": return "法语";
            case "de-DE": return "德语";
            default: return languageCode;
        }
    }
}

