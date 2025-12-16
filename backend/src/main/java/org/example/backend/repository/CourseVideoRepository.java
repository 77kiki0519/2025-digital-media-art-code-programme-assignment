package org.example.backend.repository;

import org.example.backend.entity.CourseVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseVideoRepository extends JpaRepository<CourseVideo, Long> {
    
    List<CourseVideo> findByCourseId(Long courseId);
    
    List<CourseVideo> findByChapterIdOrderByOrderNum(Long chapterId);
    
    List<CourseVideo> findByMaterialId(Long materialId);
}


