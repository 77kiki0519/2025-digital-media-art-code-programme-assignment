package org.example.backend.repository;

import org.example.backend.entity.LearningRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LearningRecordRepository extends JpaRepository<LearningRecord, Long> {
    
    List<LearningRecord> findByStudentId(Long studentId);
    
    List<LearningRecord> findByCourseId(Long courseId);
    
    List<LearningRecord> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    List<LearningRecord> findByActionType(String actionType);
}


