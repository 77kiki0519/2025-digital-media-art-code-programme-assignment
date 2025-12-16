package org.example.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 课程DTO
 */
@Data
public class CourseDTO {
    private Long id;
    private String courseName;
    private String courseCode;
    private String description;
    private Long teacherId;
    private String teacherName;
    private String coverImage;
    private Integer difficulty;
    private String category;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


