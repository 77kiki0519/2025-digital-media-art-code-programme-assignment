package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 视频字幕实体（支持多语言）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "video_subtitles")
public class VideoSubtitle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "video_id", nullable = false)
    private Long videoId;
    
    @Column(length = 10, nullable = false)
    private String language; // zh-CN, en-US, ja-JP等
    
    @Column(name = "language_name", length = 50)
    private String languageName; // 中文、English、日本語
    
    @Column(name = "subtitle_url", length = 500)
    private String subtitleUrl; // SRT/VTT文件URL
    
    @Column(name = "subtitle_content", columnDefinition = "LONGTEXT")
    private String subtitleContent; // 字幕内容（JSON格式）
    
    @Column(name = "ai_translated", columnDefinition = "TINYINT DEFAULT 0")
    private Integer aiTranslated; // 是否AI翻译：0-否，1-是
    
    @Column(name = "is_default", columnDefinition = "TINYINT DEFAULT 0")
    private Integer isDefault; // 是否默认字幕
    
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


