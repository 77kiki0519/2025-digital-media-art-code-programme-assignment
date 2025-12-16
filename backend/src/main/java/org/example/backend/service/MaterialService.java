package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.CourseMaterial;
import org.example.backend.exception.BusinessException;
import org.example.backend.repository.CourseMaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 教材制作服务（模块二）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialService {
    
    private final CourseMaterialRepository materialRepository;
    private final DeepSeekService deepSeekService;
    
    /**
     * 创建教材
     */
    @Transactional
    public CourseMaterial createMaterial(CourseMaterial material) {
        return materialRepository.save(material);
    }
    
    /**
     * 文本转PPT (AI生成)
     */
    @Transactional
    public CourseMaterial textToPPT(Long courseId, Long chapterId, String materialName, String textContent, String params) {
        // 创建教材记录
        CourseMaterial material = new CourseMaterial();
        material.setCourseId(courseId);
        material.setChapterId(chapterId);
        material.setMaterialName(materialName);
        material.setMaterialType("PPT");
        material.setContent(textContent);
        material.setAiGenerated(1);
        material.setGenerationStatus("PENDING");
        material.setGenerationParams(params);
        
        material = materialRepository.save(material);
        
        // 创建教材记录后，等待事务提交
        final Long materialId = material.getId();
        
        // 使用TransactionSynchronization确保事务提交后再执行异步任务
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
            new org.springframework.transaction.support.TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // 在事务提交后异步执行PPT生成
                    new Thread(() -> generatePPTAsync(materialId, textContent, params)).start();
                }
            }
        );
        
        return material;
    }
    
    /**
     * PPT转视频 (AI生成)
     */
    @Transactional
    public CourseMaterial pptToVideo(Long materialId, String params) {
        CourseMaterial pptMaterial = getMaterialById(materialId);
        if (!"PPT".equals(pptMaterial.getMaterialType())) {
            throw new BusinessException("只能将PPT转换为视频");
        }
        
        // 创建视频教材记录
        CourseMaterial videoMaterial = new CourseMaterial();
        videoMaterial.setCourseId(pptMaterial.getCourseId());
        videoMaterial.setChapterId(pptMaterial.getChapterId());
        videoMaterial.setMaterialName(pptMaterial.getMaterialName() + " (视频)");
        videoMaterial.setMaterialType("VIDEO");
        videoMaterial.setAiGenerated(1);
        videoMaterial.setGenerationStatus("PENDING");
        videoMaterial.setGenerationParams(params);
        
        videoMaterial = materialRepository.save(videoMaterial);
        
        // 调用AI服务生成视频 (异步处理)
        final Long videoMaterialId = videoMaterial.getId();
        new Thread(() -> generateVideoAsync(videoMaterialId, pptMaterial.getFileUrl(), params)).start();
        
        return videoMaterial;
    }
    
    /**
     * 异步生成PPT
     */
    private void generatePPTAsync(Long materialId, String textContent, String params) {
        try {
            // 更新状态为处理中
            CourseMaterial material = getMaterialById(materialId);
            material.setGenerationStatus("PROCESSING");
            materialRepository.save(material);
            
            // 调用DeepSeek AI服务生成PPT大纲
            log.info("开始生成PPT，教材ID: {}", materialId);
            String prompt = "请将以下文本内容转换为PPT大纲格式，包含标题、要点和详细说明：\n" + textContent;
            String pptOutline = deepSeekService.chat(prompt);
            
            // 生成txt格式的PPT大纲文件
            String uploadsDir = "uploads/materials";
            java.io.File dir = new java.io.File(uploadsDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            String fileName = "ppt_outline_" + materialId + ".txt";
            String filePath = uploadsDir + "/" + fileName;
            
            try (java.io.FileWriter writer = new java.io.FileWriter(filePath)) {
                writer.write("=== AI生成的PPT大纲 ===\n\n");
                writer.write("教材ID: " + materialId + "\n");
                writer.write("生成时间: " + java.time.LocalDateTime.now() + "\n\n");
                writer.write("原始内容:\n");
                writer.write(textContent + "\n\n");
                writer.write("=== AI生成的PPT结构 ===\n\n");
                writer.write(pptOutline + "\n");
            }
            
            // 估算页数（每5行算一页）
            int pages = Math.max(1, pptOutline.split("\n").length / 5);
            String pptUrl = "/api/" + filePath;
            
            // 更新教材记录
            material.setFileUrl(pptUrl);
            material.setPages(pages);
            material.setGenerationStatus("COMPLETED");
            materialRepository.save(material);
            
            log.info("PPT大纲生成完成，教材ID: {}, 文件: {}", materialId, filePath);
        } catch (Exception e) {
            log.error("PPT生成失败，教材ID: " + materialId, e);
            CourseMaterial material = getMaterialById(materialId);
            material.setGenerationStatus("FAILED");
            materialRepository.save(material);
        }
    }
    
    /**
     * 异步生成视频
     */
    private void generateVideoAsync(Long materialId, String pptUrl, String params) {
        try {
            // 更新状态为处理中
            CourseMaterial material = getMaterialById(materialId);
            material.setGenerationStatus("PROCESSING");
            materialRepository.save(material);
            
            log.info("开始将PPT转换为视频，教材ID: {}", materialId);
            
            // 生成演示文件（视频脚本）
            String uploadsDir = "uploads/materials";
            java.io.File dir = new java.io.File(uploadsDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            String fileName = "video_script_" + materialId + ".txt";
            String filePath = uploadsDir + "/" + fileName;
            int duration = 300; // 默认5分钟
            
            // 写入视频脚本到文件
            try (java.io.FileWriter writer = new java.io.FileWriter(filePath)) {
                writer.write("=== AI生成的视频讲解脚本 ===\n\n");
                writer.write("教材ID: " + materialId + "\n");
                writer.write("生成时间: " + java.time.LocalDateTime.now() + "\n");
                writer.write("预计时长: " + (duration / 60) + "分钟\n\n");
                writer.write("PPT来源: " + pptUrl + "\n");
                writer.write("生成参数: " + params + "\n\n");
                writer.write("=== 视频讲解词 ===\n\n");
                writer.write("这是AI为PPT生成的讲解词脚本。\n");
                writer.write("每一页PPT都配有详细的讲解内容。\n\n");
                writer.write("第1页：标题页\n");
                writer.write("大家好，欢迎来到本节课程...\n\n");
                writer.write("第2页：知识点一\n");
                writer.write("接下来我们学习第一个重要知识点...\n\n");
                writer.write("注意：这是演示版本，显示的是视频脚本。\n");
                writer.write("完整的视频生成需要集成FFmpeg、TTS等专业工具。\n");
            }
            
            String videoUrl = "/api/" + filePath;
            
            // 更新教材记录
            material.setFileUrl(videoUrl);
            material.setDuration(duration);
            material.setGenerationStatus("COMPLETED");
            materialRepository.save(material);
            
            log.info("视频脚本生成完成，教材ID: {}, 文件: {}", materialId, filePath);
        } catch (Exception e) {
            log.error("视频生成失败，教材ID: " + materialId, e);
            CourseMaterial material = getMaterialById(materialId);
            material.setGenerationStatus("FAILED");
            materialRepository.save(material);
        }
    }
    
    /**
     * 计算PPT页数
     */
    private int calculatePages(String content) {
        // 简单估算：每200字一页
        return Math.max(1, content.length() / 200);
    }
    
    /**
     * 获取教材详情
     */
    public CourseMaterial getMaterialById(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new BusinessException("教材不存在"));
    }
    
    /**
     * 获取课程的所有教材
     */
    public List<CourseMaterial> getMaterialsByCourseId(Long courseId) {
        return materialRepository.findByCourseId(courseId);
    }
    
    /**
     * 获取章节的教材
     */
    public List<CourseMaterial> getMaterialsByChapterId(Long chapterId) {
        return materialRepository.findByChapterId(chapterId);
    }
    
    /**
     * 删除教材
     */
    @Transactional
    public void deleteMaterial(Long id) {
        materialRepository.deleteById(id);
    }
    
    /**
     * 更新教材
     */
    @Transactional
    public CourseMaterial updateMaterial(Long id, CourseMaterial material) {
        CourseMaterial existing = getMaterialById(id);
        existing.setMaterialName(material.getMaterialName());
        existing.setContent(material.getContent());
        existing.setOrderNum(material.getOrderNum());
        return materialRepository.save(existing);
    }
}

