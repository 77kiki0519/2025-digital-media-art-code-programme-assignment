package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.PageResult;
import org.example.backend.common.Result;
import org.example.backend.entity.Course;
import org.example.backend.service.CourseService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 课程控制器
 */
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {
    
    private final CourseService courseService;
    
    /**
     * 创建课程
     */
    @PostMapping
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<Course> createCourse(@RequestBody Course course) {
        Course created = courseService.createCourse(course);
        return Result.success(created);
    }
    
    /**
     * 更新课程
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<Course> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        Course updated = courseService.updateCourse(id, course);
        return Result.success(updated);
    }
    
    /**
     * 删除课程
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public Result<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return Result.success();
    }
    
    /**
     * 获取课程详情
     */
    @GetMapping("/{id}")
    public Result<Course> getCourse(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        return Result.success(course);
    }
    
    /**
     * 分页查询课程
     */
    @GetMapping
    public Result<PageResult<Course>> getCourses(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        PageResult<Course> result = courseService.getCourses(page, size, status);
        return Result.success(result);
    }
    
    /**
     * 获取教师的课程
     */
    @GetMapping("/teacher/{teacherId}")
    public Result<List<Course>> getTeacherCourses(@PathVariable Long teacherId) {
        List<Course> courses = courseService.getTeacherCourses(teacherId);
        return Result.success(courses);
    }
    
    /**
     * 搜索课程
     */
    @GetMapping("/search")
    public Result<PageResult<Course>> searchCourses(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<Course> result = courseService.searchCourses(keyword, page, size);
        return Result.success(result);
    }
}

