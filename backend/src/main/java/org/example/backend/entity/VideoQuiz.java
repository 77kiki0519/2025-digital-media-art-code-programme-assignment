package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 视频小测验实体（模块三：线上课程）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "video_quiz")
public class VideoQuiz {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "video_id", nullable = false)
    private Long videoId;
    
    @Column(name = "time_point", nullable = false)
    private Integer timePoint; // 弹出时间点（秒）
    
    @Column(name = "question_type", nullable = false, length = 20)
    private String questionType; // SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 题目内容
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String options; // 选项（JSON格式）
    
    @Column(name = "correct_answer", nullable = false, length = 200)
    private String correctAnswer;
    
    @Column(columnDefinition = "TEXT")
    private String analysis; // 答案解析
    
    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer enabled; // 是否启用：0-禁用，1-启用
    
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


