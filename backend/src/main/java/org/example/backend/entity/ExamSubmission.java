package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考试提交实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exam_submissions")
public class ExamSubmission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "exam_id", nullable = false)
    private Long examId;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "submit_time")
    private LocalDateTime submitTime;
    
    @Column
    private Integer duration;
    
    @Column(name = "total_score", precision = 5, scale = 2)
    private BigDecimal totalScore;
    
    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer status;
    
    @Column(name = "ai_scored", columnDefinition = "TINYINT DEFAULT 0")
    private Integer aiScored;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


