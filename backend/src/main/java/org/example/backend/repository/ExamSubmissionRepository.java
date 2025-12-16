package org.example.backend.repository;

import org.example.backend.entity.ExamSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 考试提交Repository
 */
@Repository
public interface ExamSubmissionRepository extends JpaRepository<ExamSubmission, Long> {
    
    /**
     * 根据考试ID查找所有提交
     */
    List<ExamSubmission> findByExamId(Long examId);
    
    /**
     * 根据学生ID查找所有提交
     */
    List<ExamSubmission> findByStudentId(Long studentId);
    
    /**
     * 根据考试ID和学生ID查找提交
     */
    Optional<ExamSubmission> findByExamIdAndStudentId(Long examId, Long studentId);
}

