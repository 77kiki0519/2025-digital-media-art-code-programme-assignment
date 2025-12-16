package org.example.backend.repository;

import org.example.backend.entity.VideoSubtitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoSubtitleRepository extends JpaRepository<VideoSubtitle, Long> {
    
    List<VideoSubtitle> findByVideoId(Long videoId);
    
    Optional<VideoSubtitle> findByVideoIdAndLanguage(Long videoId, String language);
    
    Optional<VideoSubtitle> findByVideoIdAndIsDefault(Long videoId, Integer isDefault);
}


