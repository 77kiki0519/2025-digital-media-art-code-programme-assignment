package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 知识点实体（视频内嵌知识点）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "knowledge_points")
public class KnowledgePoint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "video_id", nullable = false)
    private Long videoId;
    
    @Column(name = "time_point", nullable = false)
    private Integer timePoint; // 弹出时间点（秒）
    
    @Column(nullable = false, length = 200)
    private String title; // 知识点标题
    
    @Column(columnDefinition = "TEXT")
    private String content; // 知识点详细内容
    
    @Column(name = "point_type", length = 20)
    private String pointType; // INFO-提示，IMPORTANT-重点，WARNING-注意
    
    @Column(name = "related_url", length = 500)
    private String relatedUrl; // 相关资料链接
    
    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer enabled; // 是否启用
    
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


