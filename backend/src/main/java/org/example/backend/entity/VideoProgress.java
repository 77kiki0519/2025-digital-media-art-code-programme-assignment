package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 视频播放进度实体（模块三：线上课程）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "video_progress", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"video_id", "student_id"}))
public class VideoProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "video_id", nullable = false)
    private Long videoId;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer progress; // 播放进度（秒）
    
    @Column
    private Integer duration; // 视频总时长（秒）
    
    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer completed; // 是否完成：0-未完成，1-已完成
    
    @Column(name = "last_position")
    private Integer lastPosition; // 最后播放位置（秒）
    
    @Column(name = "play_times", columnDefinition = "INT DEFAULT 0")
    private Integer playTimes; // 播放次数
    
    @Column(name = "last_play_time")
    private LocalDateTime lastPlayTime;
    
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


