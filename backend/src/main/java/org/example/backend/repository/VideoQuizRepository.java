package org.example.backend.repository;

import org.example.backend.entity.VideoQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VideoQuizRepository extends JpaRepository<VideoQuiz, Long> {
    
    List<VideoQuiz> findByVideoIdAndEnabled(Long videoId, Integer enabled);
    
    List<VideoQuiz> findByVideoIdOrderByTimePoint(Long videoId);
}


