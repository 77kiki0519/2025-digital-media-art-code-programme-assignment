package org.example.backend.repository;

import org.example.backend.entity.VideoNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VideoNoteRepository extends JpaRepository<VideoNote, Long> {
    
    List<VideoNote> findByVideoIdAndStudentId(Long videoId, Long studentId);
    
    List<VideoNote> findByStudentId(Long studentId);
    
    List<VideoNote> findByVideoId(Long videoId);
}


