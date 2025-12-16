package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 视频弹幕实体（模块三：线上课程）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "video_danmaku")
public class VideoDanmaku {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "video_id", nullable = false)
    private Long videoId;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(name = "time_point", nullable = false)
    private Integer timePoint; // 时间点（秒）
    
    @Column(nullable = false, length = 500)
    private String content; // 弹幕内容
    
    @Column(length = 20)
    private String color = "#FFFFFF"; // 弹幕颜色
    
    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer type; // 弹幕类型：0-滚动，1-顶部，2-底部
    
    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer status; // 状态：0-隐藏，1-显示
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}


