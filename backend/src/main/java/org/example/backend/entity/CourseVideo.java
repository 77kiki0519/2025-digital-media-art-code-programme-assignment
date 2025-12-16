package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 课程视频实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course_videos")
public class CourseVideo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    
    @Column(name = "chapter_id")
    private Long chapterId;
    
    @Column(name = "material_id")
    private Long materialId;
    
    @Column(nullable = false, length = 200)
    private String title; // 视频标题
    
    @Column(name = "video_url", nullable = false, length = 500)
    private String videoUrl;
    
    @Column(name = "subtitle_url", length = 500)
    private String subtitleUrl;
    
    @Column(name = "cover_image", length = 500)
    private String coverImage;
    
    @Column
    private Integer duration;
    
    @Column(name = "order_num")
    private Integer orderNum;
    
    @Column(name = "ai_generated", columnDefinition = "TINYINT DEFAULT 0")
    private Integer aiGenerated;
    
    @Column(name = "allow_speed", columnDefinition = "TINYINT DEFAULT 1")
    private Integer allowSpeed;
    
    @Column(name = "allow_danmaku", columnDefinition = "TINYINT DEFAULT 1")
    private Integer allowDanmaku;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


