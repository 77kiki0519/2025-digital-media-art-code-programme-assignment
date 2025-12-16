package org.example.backend.util;

import org.apache.poi.xslf.usermodel.*;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * PPT生成工具类 - 使用Apache POI生成真实的PPTX文件
 */
public class PPTGenerator {
    
    /**
     * 生成PPTX文件
     * 
     * @param content AI生成的PPT大纲内容
     * @param outputPath 输出文件路径
     * @return 页数
     */
    public static int generatePPTX(String content, String outputPath) throws IOException {
        XMLSlideShow ppt = new XMLSlideShow();
        
        // 解析AI生成的内容并创建幻灯片
        String[] lines = content.split("\n");
        int slideCount = 0;
        
        // 第一页：标题页
        XSLFSlide titleSlide = ppt.createSlide();
        addTitleSlide(titleSlide, "AI生成的教学课件", "基于DeepSeek AI智能生成");
        slideCount++;
        
        // 解析内容创建幻灯片
        String currentTitle = "";
        StringBuilder currentContent = new StringBuilder();
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            // 识别标题行（以#开头或包含"幻灯片"）
            if (line.startsWith("#") || line.contains("幻灯片") || line.contains("第") && line.contains("页")) {
                // 如果已有内容，创建上一页
                if (!currentTitle.isEmpty() && currentContent.length() > 0) {
                    XSLFSlide slide = ppt.createSlide();
                    addContentSlide(slide, currentTitle, currentContent.toString());
                    slideCount++;
                    currentContent = new StringBuilder();
                }
                // 提取新标题
                currentTitle = line.replaceAll("[#*-]", "").trim();
            } else if (line.startsWith("- ") || line.startsWith("* ") || line.startsWith("• ")) {
                // 要点内容
                currentContent.append(line.replaceFirst("^[\\-\\*•]\\s*", "")).append("\n");
            } else if (!line.startsWith("=") && !line.contains("---")) {
                // 普通内容
                if (line.length() < 100) { // 避免太长的文本
                    currentContent.append(line).append("\n");
                }
            }
        }
        
        // 添加最后一页
        if (!currentTitle.isEmpty() && currentContent.length() > 0) {
            XSLFSlide slide = ppt.createSlide();
            addContentSlide(slide, currentTitle, currentContent.toString());
            slideCount++;
        }
        
        // 如果内容解析失败，至少创建一个内容页
        if (slideCount <= 1) {
            XSLFSlide slide = ppt.createSlide();
            addContentSlide(slide, "课程内容", content.length() > 500 ? content.substring(0, 500) : content);
            slideCount++;
        }
        
        // 保存文件
        try (FileOutputStream out = new FileOutputStream(outputPath)) {
            ppt.write(out);
        }
        
        ppt.close();
        return slideCount;
    }
    
    /**
     * 添加标题页
     */
    private static void addTitleSlide(XSLFSlide slide, String title, String subtitle) {
        // 标题
        XSLFTextBox titleBox = slide.createTextBox();
        titleBox.setAnchor(new Rectangle2D.Double(50, 150, 600, 100));
        XSLFTextParagraph titlePara = titleBox.addNewTextParagraph();
        titlePara.setTextAlign(org.apache.poi.sl.usermodel.TextParagraph.TextAlign.CENTER);
        XSLFTextRun titleRun = titlePara.addNewTextRun();
        titleRun.setText(title);
        titleRun.setFontSize(44.0);
        titleRun.setBold(true);
        titleRun.setFontColor(new Color(31, 78, 121));
        
        // 副标题
        XSLFTextBox subtitleBox = slide.createTextBox();
        subtitleBox.setAnchor(new Rectangle2D.Double(50, 280, 600, 60));
        XSLFTextParagraph subtitlePara = subtitleBox.addNewTextParagraph();
        subtitlePara.setTextAlign(org.apache.poi.sl.usermodel.TextParagraph.TextAlign.CENTER);
        XSLFTextRun subtitleRun = subtitlePara.addNewTextRun();
        subtitleRun.setText(subtitle);
        subtitleRun.setFontSize(24.0);
        subtitleRun.setFontColor(new Color(89, 89, 89));
    }
    
    /**
     * 添加内容页
     */
    private static void addContentSlide(XSLFSlide slide, String title, String content) {
        // 标题
        XSLFTextBox titleBox = slide.createTextBox();
        titleBox.setAnchor(new Rectangle2D.Double(50, 40, 600, 60));
        XSLFTextParagraph titlePara = titleBox.addNewTextParagraph();
        XSLFTextRun titleRun = titlePara.addNewTextRun();
        titleRun.setText(title);
        titleRun.setFontSize(32.0);
        titleRun.setBold(true);
        titleRun.setFontColor(new Color(31, 78, 121));
        
        // 内容
        XSLFTextBox contentBox = slide.createTextBox();
        contentBox.setAnchor(new Rectangle2D.Double(50, 120, 600, 350));
        
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            XSLFTextParagraph para = contentBox.addNewTextParagraph();
            para.setBullet(true);
            para.setIndentLevel(0);
            XSLFTextRun run = para.addNewTextRun();
            run.setText(line.trim());
            run.setFontSize(18.0);
            run.setFontColor(Color.BLACK);
        }
    }
}

