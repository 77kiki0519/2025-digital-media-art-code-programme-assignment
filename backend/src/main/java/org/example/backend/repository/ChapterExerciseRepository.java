package org.example.backend.repository;

import org.example.backend.entity.ChapterExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChapterExerciseRepository extends JpaRepository<ChapterExercise, Long> {
    
    List<ChapterExercise> findByChapterId(Long chapterId);
    
    List<ChapterExercise> findByChapterIdAndStatus(Long chapterId, Integer status);
}


