package org.example.backend.repository;

import org.example.backend.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 提问Repository
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    Page<Question> findByCourseId(Long courseId, Pageable pageable);
    
    Page<Question> findByStudentId(Long studentId, Pageable pageable);
    
    Page<Question> findByStudentIdAndCourseId(Long studentId, Long courseId, Pageable pageable);
    
    Page<Question> findByCourseIdAndStatus(Long courseId, Integer status, Pageable pageable);
}


