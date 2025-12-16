package org.example.backend.repository;

import org.example.backend.entity.ReportSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 报告提交Repository
 */
@Repository
public interface ReportSubmissionRepository extends JpaRepository<ReportSubmission, Long> {
    
    /**
     * 根据报告ID查找所有提交
     */
    List<ReportSubmission> findByReportId(Long reportId);
    
    /**
     * 根据学生ID查找所有提交
     */
    List<ReportSubmission> findByStudentId(Long studentId);
    
    /**
     * 根据报告ID和学生ID查找提交
     */
    Optional<ReportSubmission> findByReportIdAndStudentId(Long reportId, Long studentId);
}

