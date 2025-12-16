package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 报告提交实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "report_submissions")
public class ReportSubmission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "report_id", nullable = false)
    private Long reportId;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(length = 200)
    private String title;
    
    @Column(columnDefinition = "LONGTEXT")
    private String content;
    
    @Column(name = "file_url", length = 500)
    private String fileUrl;
    
    @Column(name = "file_type", length = 20)
    private String fileType;
    
    @Column(name = "word_count")
    private Integer wordCount;
    
    @Column(name = "submit_time")
    private LocalDateTime submitTime;
    
    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer status;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (submitTime == null) {
            submitTime = LocalDateTime.now();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


