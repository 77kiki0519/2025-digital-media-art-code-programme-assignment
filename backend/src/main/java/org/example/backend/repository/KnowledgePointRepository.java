package org.example.backend.repository;

import org.example.backend.entity.KnowledgePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KnowledgePointRepository extends JpaRepository<KnowledgePoint, Long> {
    
    List<KnowledgePoint> findByVideoIdAndEnabled(Long videoId, Integer enabled);
    
    List<KnowledgePoint> findByVideoIdOrderByTimePoint(Long videoId);
}


