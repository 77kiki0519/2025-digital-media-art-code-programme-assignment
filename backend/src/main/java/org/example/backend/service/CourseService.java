package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.PageResult;
import org.example.backend.entity.Course;
import org.example.backend.exception.BusinessException;
import org.example.backend.repository.CourseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 课程服务
 */
@Service
@RequiredArgsConstructor
public class CourseService {
    
    private final CourseRepository courseRepository;
    
    /**
     * 创建课程
     */
    @Transactional
    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }
    
    /**
     * 更新课程
     */
    @Transactional
    public Course updateCourse(Long id, Course course) {
        Course existingCourse = getCourseById(id);
        existingCourse.setCourseName(course.getCourseName());
        existingCourse.setCourseCode(course.getCourseCode());
        existingCourse.setDescription(course.getDescription());
        existingCourse.setCoverImage(course.getCoverImage());
        existingCourse.setDifficulty(course.getDifficulty());
        existingCourse.setCategory(course.getCategory());
        existingCourse.setStatus(course.getStatus());
        return courseRepository.save(existingCourse);
    }
    
    /**
     * 删除课程
     */
    @Transactional
    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }
    
    /**
     * 根据ID获取课程
     */
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("课程不存在"));
    }
    
    /**
     * 分页查询课程
     */
    public PageResult<Course> getCourses(Integer page, Integer size, Integer status) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Course> coursePage;
        
        if (status != null) {
            coursePage = courseRepository.findByStatus(status, pageable);
        } else {
            coursePage = courseRepository.findAll(pageable);
        }
        
        return new PageResult<>(
                coursePage.getContent(),
                coursePage.getTotalElements(),
                (long) page,
                (long) size
        );
    }
    
    /**
     * 获取教师的课程
     */
    public List<Course> getTeacherCourses(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }
    
    /**
     * 搜索课程
     */
    public PageResult<Course> searchCourses(String keyword, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Course> coursePage = courseRepository.findByCourseNameContaining(keyword, pageable);
        
        return new PageResult<>(
                coursePage.getContent(),
                coursePage.getTotalElements(),
                (long) page,
                (long) size
        );
    }
}

