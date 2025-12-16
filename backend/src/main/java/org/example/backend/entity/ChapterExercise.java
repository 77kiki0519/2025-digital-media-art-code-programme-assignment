package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 章节练习实体（模块三：线上课程）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chapter_exercises")
public class ChapterExercise {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;
    
    @Column(name = "exercise_name", nullable = false, length = 200)
    private String exerciseName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "total_score", columnDefinition = "INT DEFAULT 100")
    private Integer totalScore;
    
    @Column(name = "pass_score", columnDefinition = "INT DEFAULT 60")
    private Integer passScore;
    
    @Column(name = "time_limit")
    private Integer timeLimit; // 时间限制（分钟）
    
    @Column(name = "allow_retry", columnDefinition = "TINYINT DEFAULT 1")
    private Integer allowRetry;
    
    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer status; // 状态：0-关闭，1-开放
    
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


