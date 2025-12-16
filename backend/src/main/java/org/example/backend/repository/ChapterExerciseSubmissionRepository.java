package org.example.backend.repository;

import org.example.backend.entity.ChapterExerciseSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChapterExerciseSubmissionRepository extends JpaRepository<ChapterExerciseSubmission, Long> {
    
    List<ChapterExerciseSubmission> findByExerciseIdAndStudentId(Long exerciseId, Long studentId);
    
    List<ChapterExerciseSubmission> findByStudentId(Long studentId);
    
    List<ChapterExerciseSubmission> findByExerciseId(Long exerciseId);
}


