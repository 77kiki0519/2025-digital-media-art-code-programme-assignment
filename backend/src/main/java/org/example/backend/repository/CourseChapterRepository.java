package org.example.backend.repository;

import org.example.backend.entity.CourseChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseChapterRepository extends JpaRepository<CourseChapter, Long> {
    
    List<CourseChapter> findByCourseIdOrderByChapterOrder(Long courseId);
    
    List<CourseChapter> findByParentId(Long parentId);
    
    List<CourseChapter> findByCourseIdAndParentIdIsNull(Long courseId);
}


