package org.example.backend.repository;

import org.example.backend.entity.CourseMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseMaterialRepository extends JpaRepository<CourseMaterial, Long> {
    
    List<CourseMaterial> findByCourseId(Long courseId);
    
    List<CourseMaterial> findByChapterId(Long chapterId);
    
    List<CourseMaterial> findByCourseIdAndMaterialType(Long courseId, String materialType);
    
    List<CourseMaterial> findByGenerationStatus(String status);
}


