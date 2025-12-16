package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.Result;
import org.example.backend.entity.Report;
import org.example.backend.entity.ReportSubmission;
import org.example.backend.repository.ReportSubmissionRepository;
import org.example.backend.repository.UserRepository;
import org.example.backend.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报告控制器
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    
    private final ReportService reportService;
    private final ReportSubmissionRepository reportSubmissionRepository;
    private final UserRepository userRepository;
    
    /**
     * 创建报告作业
     */
    @PostMapping
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<Report> createReport(@RequestBody Report report) {
        Report created = reportService.createReport(report);
        return Result.success(created);
    }
    
    /**
     * 更新报告作业
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<Report> updateReport(@PathVariable Long id, @RequestBody Report report) {
        Report updated = reportService.updateReport(id, report);
        return Result.success(updated);
    }
    
    /**
     * 删除报告作业
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return Result.success();
    }
    
    /**
     * 获取报告详情
     */
    @GetMapping("/{id}")
    public Result<Report> getReport(@PathVariable Long id) {
        Report report = reportService.getReportById(id);
        return Result.success(report);
    }
    
    /**
     * 获取课程的所有报告作业
     */
    @GetMapping("/course/{courseId}")
    public Result<List<Report>> getCourseReports(@PathVariable Long courseId) {
        List<Report> reports = reportService.getCourseReports(courseId);
        return Result.success(reports);
    }
    
    /**
     * AI批改报告
     */
    @PostMapping("/review-ai")
    public Result<Map<String, Object>> reviewReportWithAI(@RequestBody Map<String, String> request) {
        String requirement = request.get("requirement");
        String content = request.get("content");
        Map<String, Object> result = reportService.reviewReportWithAI(requirement, content);
        return Result.success(result);
    }
    
    /**
     * 获取报告的所有提交记录（教师）
     */
    @GetMapping("/{reportId}/submissions")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<List<Map<String, Object>>> getReportSubmissions(@PathVariable Long reportId) {
        List<ReportSubmission> submissions = reportSubmissionRepository.findByReportId(reportId);
        List<Map<String, Object>> result = submissions.stream().map(submission -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", submission.getId());
            map.put("reportId", submission.getReportId());
            map.put("studentId", submission.getStudentId());
            map.put("title", submission.getTitle());
            map.put("content", submission.getContent());
            map.put("fileUrl", submission.getFileUrl());
            map.put("fileType", submission.getFileType());
            map.put("wordCount", submission.getWordCount());
            map.put("submitTime", submission.getSubmitTime());
            map.put("status", submission.getStatus());
            // 添加学生信息
            userRepository.findById(submission.getStudentId()).ifPresent(user -> {
                map.put("studentName", user.getRealName());
                map.put("studentNo", user.getStudentNo());
            });
            return map;
        }).collect(Collectors.toList());
        return Result.success(result);
    }
    
    /**
     * 提交报告（学生）- 只能提交一次
     */
    @PostMapping("/{reportId}/submit")
    public Result<ReportSubmission> submitReport(
            @PathVariable Long reportId,
            @RequestBody ReportSubmission submission) {
        submission.setReportId(reportId);
        
        // 检查是否已有提交记录
        ReportSubmission existing = reportSubmissionRepository
                .findByReportIdAndStudentId(reportId, submission.getStudentId())
                .orElse(null);
        
        if (existing != null) {
            // 已提交，不允许重复提交
            return Result.error("您已经提交过该报告，不能重复提交");
        } else {
            // 新建提交记录
            submission.setStatus(1);
            submission.setSubmitTime(java.time.LocalDateTime.now());
            ReportSubmission saved = reportSubmissionRepository.save(submission);
            return Result.success(saved);
        }
    }
}

