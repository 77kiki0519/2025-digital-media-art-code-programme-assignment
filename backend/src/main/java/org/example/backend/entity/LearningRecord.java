package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 学习记录实体（模块三：课程统计）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "learning_records")
public class LearningRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    
    @Column(name = "chapter_id")
    private Long chapterId;
    
    @Column(name = "video_id")
    private Long videoId;
    
    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType; // VIEW, PAUSE, COMPLETE, NOTE, QUIZ
    
    @Column(name = "action_time")
    private LocalDateTime actionTime;
    
    @Column
    private Integer duration; // 持续时长（秒）
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (actionTime == null) {
            actionTime = LocalDateTime.now();
        }
    }
}


