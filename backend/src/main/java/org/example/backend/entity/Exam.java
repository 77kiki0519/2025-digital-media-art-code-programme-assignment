package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 考试实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exams")
public class Exam {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    
    @Column(name = "exam_name", nullable = false, length = 100)
    private String examName;
    
    @Column(name = "exam_type", length = 20)
    private String examType;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "total_score")
    private Integer totalScore;
    
    @Column
    private Integer duration;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "ai_generated", columnDefinition = "TINYINT DEFAULT 0")
    private Integer aiGenerated;
    
    @Column(name = "anti_cheat", columnDefinition = "TINYINT DEFAULT 0")
    private Integer antiCheat;
    
    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer status;
    
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


