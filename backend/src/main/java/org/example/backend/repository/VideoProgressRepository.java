package org.example.backend.repository;

import org.example.backend.entity.VideoProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoProgressRepository extends JpaRepository<VideoProgress, Long> {
    
    Optional<VideoProgress> findByVideoIdAndStudentId(Long videoId, Long studentId);
    
    List<VideoProgress> findByStudentId(Long studentId);
    
    List<VideoProgress> findByVideoId(Long videoId);
    
    List<VideoProgress> findByStudentIdAndCompleted(Long studentId, Integer completed);
}


