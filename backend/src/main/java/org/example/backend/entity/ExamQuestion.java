package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 试题实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exam_questions")
public class ExamQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "exam_id", nullable = false)
    private Long examId;
    
    @Column(name = "question_type", nullable = false, length = 20)
    private String questionType;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(columnDefinition = "JSON")
    private String options;
    
    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;
    
    @Column(columnDefinition = "TINYINT")
    private Integer difficulty;
    
    @Column(name = "knowledge_point", length = 200)
    private String knowledgePoint;
    
    @Column(columnDefinition = "TEXT")
    private String analysis;
    
    @Column(name = "question_order")
    private Integer questionOrder;
    
    @Column(name = "ai_generated", columnDefinition = "TINYINT DEFAULT 0")
    private Integer aiGenerated;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}


