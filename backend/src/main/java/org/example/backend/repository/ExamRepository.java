package org.example.backend.repository;

import org.example.backend.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 考试Repository
 */
@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    
    List<Exam> findByCourseId(Long courseId);
    
    List<Exam> findByCourseIdAndStatus(Long courseId, Integer status);
}


