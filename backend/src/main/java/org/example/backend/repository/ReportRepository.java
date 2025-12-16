package org.example.backend.repository;

import org.example.backend.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 报告Repository
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    List<Report> findByCourseId(Long courseId);
    
    List<Report> findByCourseIdAndStatus(Long courseId, Integer status);
}


