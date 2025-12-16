package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 回答实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "answers")
public class Answer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "question_id", nullable = false)
    private Long questionId;
    
    @Column(name = "answerer_id")
    private Long answererId;
    
    @Column(name = "answerer_type", length = 20, nullable = false)
    private String answererType;  // TEACHER, AI
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

