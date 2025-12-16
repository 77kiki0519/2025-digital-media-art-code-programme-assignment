package org.example.backend.repository;

import org.example.backend.entity.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 试题Repository
 */
@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {
    
    List<ExamQuestion> findByExamIdOrderByQuestionOrder(Long examId);
}


