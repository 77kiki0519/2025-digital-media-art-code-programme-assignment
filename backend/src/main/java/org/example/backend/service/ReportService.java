package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.Report;
import org.example.backend.entity.ReportSubmission;
import org.example.backend.exception.BusinessException;
import org.example.backend.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

/**
 * 报告服务
 */
@Service
@RequiredArgsConstructor
public class ReportService {
    
    private final ReportRepository reportRepository;
    private final DeepSeekService deepSeekService;
    
    /**
     * 创建报告作业
     */
    @Transactional
    public Report createReport(Report report) {
        return reportRepository.save(report);
    }
    
    /**
     * 更新报告作业
     */
    @Transactional
    public Report updateReport(Long id, Report report) {
        Report existing = getReportById(id);
        existing.setReportName(report.getReportName());
        existing.setDescription(report.getDescription());
        existing.setRequirements(report.getRequirements());
        existing.setWordLimit(report.getWordLimit());
        existing.setDeadline(report.getDeadline());
        existing.setTotalScore(report.getTotalScore());
        existing.setStatus(report.getStatus());
        return reportRepository.save(existing);
    }
    
    /**
     * 删除报告作业
     */
    @Transactional
    public void deleteReport(Long id) {
        reportRepository.deleteById(id);
    }
    
    /**
     * 获取报告详情
     */
    public Report getReportById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new BusinessException("报告作业不存在"));
    }
    
    /**
     * 获取课程的所有报告作业
     */
    public List<Report> getCourseReports(Long courseId) {
        return reportRepository.findByCourseId(courseId);
    }
    
    /**
     * AI批改报告
     */
    public Map<String, Object> reviewReportWithAI(String reportRequirement, String reportContent) {
        return deepSeekService.reviewReport(reportRequirement, reportContent);
    }
}


