package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 课程教材实体（模块二：教材制作）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course_materials")
public class CourseMaterial {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    
    @Column(name = "chapter_id")
    private Long chapterId;
    
    @Column(name = "material_name", nullable = false, length = 200)
    private String materialName;
    
    @Column(name = "material_type", nullable = false, length = 20)
    private String materialType; // TEXT, PPT, VIDEO
    
    @Column(columnDefinition = "LONGTEXT")
    private String content; // 文本内容（用于文本转PPT）
    
    @Column(name = "file_url", length = 500)
    private String fileUrl;
    
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;
    
    @Column
    private Integer pages; // PPT页数
    
    @Column
    private Integer duration; // 视频时长（秒）
    
    @Column(name = "ai_generated", columnDefinition = "TINYINT DEFAULT 0")
    private Integer aiGenerated;
    
    @Column(name = "generation_status", length = 20)
    private String generationStatus; // PENDING, PROCESSING, COMPLETED, FAILED
    
    @Column(name = "generation_params", columnDefinition = "TEXT")
    private String generationParams; // JSON格式
    
    @Column(name = "order_num")
    private Integer orderNum;
    
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


