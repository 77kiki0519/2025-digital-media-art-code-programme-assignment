package org.example.backend.repository;

import org.example.backend.entity.ChapterExerciseQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChapterExerciseQuestionRepository extends JpaRepository<ChapterExerciseQuestion, Long> {
    
    List<ChapterExerciseQuestion> findByExerciseIdOrderByQuestionOrder(Long exerciseId);
}


