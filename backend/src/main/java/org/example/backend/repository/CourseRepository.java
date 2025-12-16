package org.example.backend.repository;

import org.example.backend.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 课程Repository
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    Page<Course> findByStatus(Integer status, Pageable pageable);
    
    List<Course> findByTeacherId(Long teacherId);
    
    Page<Course> findByTeacherIdAndStatus(Long teacherId, Integer status, Pageable pageable);
    
    Page<Course> findByCourseNameContaining(String keyword, Pageable pageable);
}


