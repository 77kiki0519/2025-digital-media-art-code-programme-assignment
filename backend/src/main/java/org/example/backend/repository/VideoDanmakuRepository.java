package org.example.backend.repository;

import org.example.backend.entity.VideoDanmaku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VideoDanmakuRepository extends JpaRepository<VideoDanmaku, Long> {
    
    List<VideoDanmaku> findByVideoIdAndStatus(Long videoId, Integer status);
    
    List<VideoDanmaku> findByStudentId(Long studentId);
    
    List<VideoDanmaku> findByVideoIdOrderByTimePoint(Long videoId);
}


