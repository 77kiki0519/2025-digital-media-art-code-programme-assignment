package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 章节练习题目实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chapter_exercise_questions")
public class ChapterExerciseQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "exercise_id", nullable = false)
    private Long exerciseId;
    
    @Column(name = "question_type", nullable = false, length = 20)
    private String questionType; // SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(columnDefinition = "TEXT")
    private String options; // JSON格式
    
    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;
    
    @Column(columnDefinition = "TEXT")
    private String analysis;
    
    @Column(name = "question_order")
    private Integer questionOrder;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}


