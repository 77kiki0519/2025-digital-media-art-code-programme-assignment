package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 视频笔记实体（模块三：线上课程）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "video_notes")
public class VideoNote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "video_id", nullable = false)
    private Long videoId;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(name = "time_point", nullable = false)
    private Integer timePoint; // 时间点（秒）
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 笔记内容
    
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


