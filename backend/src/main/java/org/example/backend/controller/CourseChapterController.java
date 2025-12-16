package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.Result;
import org.example.backend.entity.CourseChapter;
import org.example.backend.entity.CourseVideo;
import org.example.backend.repository.CourseChapterRepository;
import org.example.backend.repository.CourseVideoRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程章节和视频管理控制器
 */
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
@CrossOrigin
public class CourseChapterController {
    
    private final CourseChapterRepository chapterRepository;
    private final CourseVideoRepository videoRepository;
    
    /**
     * 获取课程的章节和视频
     */
    @GetMapping("/{courseId}/chapters")
    public Result<List<Map<String, Object>>> getCourseChapters(@PathVariable Long courseId) {
        List<CourseChapter> chapters = chapterRepository.findByCourseIdOrderByChapterOrder(courseId);
        
        List<Map<String, Object>> result = chapters.stream().map(chapter -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", chapter.getId());
            map.put("chapterName", chapter.getChapterName());
            map.put("chapterOrder", chapter.getChapterOrder());
            map.put("description", chapter.getDescription());
            
            // 加载该章节下的视频
            List<CourseVideo> videos = videoRepository.findByChapterIdOrderByOrderNum(chapter.getId());
            map.put("videos", videos);
            
            return map;
        }).toList();
        
        return Result.success(result);
    }
    
    /**
     * 创建章节
     */
    @PostMapping("/chapters")
    public Result<CourseChapter> createChapter(@RequestBody CourseChapter chapter) {
        CourseChapter saved = chapterRepository.save(chapter);
        return Result.success(saved);
    }
    
    /**
     * 更新章节
     */
    @PutMapping("/chapters/{id}")
    public Result<CourseChapter> updateChapter(@PathVariable Long id, @RequestBody CourseChapter chapter) {
        CourseChapter existing = chapterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("章节不存在"));
        
        existing.setChapterName(chapter.getChapterName());
        existing.setChapterOrder(chapter.getChapterOrder());
        existing.setDescription(chapter.getDescription());
        
        CourseChapter updated = chapterRepository.save(existing);
        return Result.success(updated);
    }
    
    /**
     * 删除章节
     */
    @DeleteMapping("/chapters/{id}")
    public Result<Void> deleteChapter(@PathVariable Long id) {
        chapterRepository.deleteById(id);
        return Result.success();
    }
    
    /**
     * 创建视频
     */
    @PostMapping("/videos")
    public Result<CourseVideo> createVideo(@RequestBody CourseVideo video) {
        CourseVideo saved = videoRepository.save(video);
        return Result.success(saved);
    }
    
    /**
     * 更新视频
     */
    @PutMapping("/videos/{id}")
    public Result<CourseVideo> updateVideo(@PathVariable Long id, @RequestBody CourseVideo video) {
        CourseVideo existing = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("视频不存在"));
        
        existing.setTitle(video.getTitle());
        existing.setVideoUrl(video.getVideoUrl());
        existing.setSubtitleUrl(video.getSubtitleUrl());
        existing.setCoverImage(video.getCoverImage());
        existing.setDuration(video.getDuration());
        existing.setOrderNum(video.getOrderNum());
        existing.setAllowSpeed(video.getAllowSpeed());
        existing.setAllowDanmaku(video.getAllowDanmaku());
        
        CourseVideo updated = videoRepository.save(existing);
        return Result.success(updated);
    }
    
    /**
     * 删除视频
     */
    @DeleteMapping("/videos/{id}")
    public Result<Void> deleteVideo(@PathVariable Long id) {
        videoRepository.deleteById(id);
        return Result.success();
    }
}

