package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 章节练习提交实体（模块三：线上课程）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chapter_exercise_submissions")
public class ChapterExerciseSubmission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "exercise_id", nullable = false)
    private Long exerciseId;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(columnDefinition = "TEXT")
    private String answers; // 学生答案（JSON格式）
    
    @Column(precision = 5, scale = 2)
    private BigDecimal score = BigDecimal.ZERO;
    
    @Column(name = "is_passed", columnDefinition = "TINYINT DEFAULT 0")
    private Integer isPassed;
    
    @Column(name = "submit_time")
    private LocalDateTime submitTime;
    
    @Column
    private Integer duration; // 用时（分钟）
    
    @Column(name = "retry_count", columnDefinition = "INT DEFAULT 0")
    private Integer retryCount;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        submitTime = LocalDateTime.now();
    }
}


