package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.Result;
import org.example.backend.entity.CourseMaterial;
import org.example.backend.service.MaterialService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 教材制作控制器（模块二）
 */
@RestController
@RequestMapping("/materials")
@RequiredArgsConstructor
public class MaterialController {
    
    private final MaterialService materialService;
    
    /**
     * 创建教材
     */
    @PostMapping
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<CourseMaterial> createMaterial(@RequestBody CourseMaterial material) {
        CourseMaterial created = materialService.createMaterial(material);
        return Result.success(created);
    }
    
    /**
     * 文本转PPT
     */
    @PostMapping("/text-to-ppt")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<CourseMaterial> textToPPT(@RequestBody Map<String, Object> request) {
        Long courseId = Long.valueOf(request.get("courseId").toString());
        Long chapterId = request.get("chapterId") != null ? Long.valueOf(request.get("chapterId").toString()) : null;
        String materialName = request.get("materialName").toString();
        String textContent = request.get("textContent").toString();
        String params = request.get("params") != null ? request.get("params").toString() : "{}";
        
        CourseMaterial material = materialService.textToPPT(courseId, chapterId, materialName, textContent, params);
        return Result.success(material);
    }
    
    /**
     * PPT转视频
     */
    @PostMapping("/ppt-to-video")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<CourseMaterial> pptToVideo(@RequestBody Map<String, Object> request) {
        Long materialId = Long.valueOf(request.get("materialId").toString());
        String params = request.get("params") != null ? request.get("params").toString() : "{}";
        
        CourseMaterial material = materialService.pptToVideo(materialId, params);
        return Result.success(material);
    }
    
    /**
     * 获取教材详情
     */
    @GetMapping("/{id}")
    public Result<CourseMaterial> getMaterial(@PathVariable Long id) {
        CourseMaterial material = materialService.getMaterialById(id);
        return Result.success(material);
    }
    
    /**
     * 获取课程的教材列表
     */
    @GetMapping("/course/{courseId}")
    public Result<List<CourseMaterial>> getMaterialsByCourse(@PathVariable Long courseId) {
        List<CourseMaterial> materials = materialService.getMaterialsByCourseId(courseId);
        return Result.success(materials);
    }
    
    /**
     * 获取章节的教材列表
     */
    @GetMapping("/chapter/{chapterId}")
    public Result<List<CourseMaterial>> getMaterialsByChapter(@PathVariable Long chapterId) {
        List<CourseMaterial> materials = materialService.getMaterialsByChapterId(chapterId);
        return Result.success(materials);
    }
    
    /**
     * 更新教材
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<CourseMaterial> updateMaterial(@PathVariable Long id, @RequestBody CourseMaterial material) {
        CourseMaterial updated = materialService.updateMaterial(id, material);
        return Result.success(updated);
    }
    
    /**
     * 删除教材
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<Void> deleteMaterial(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return Result.success();
    }
}


