package org.example.backend.repository;

import org.example.backend.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 回答仓库
 */
@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    
    /**
     * 根据问题ID查找回答
     */
    List<Answer> findByQuestionIdOrderByCreatedAtAsc(Long questionId);
    
    /**
     * 根据问题ID查找第一个回答
     */
    Optional<Answer> findFirstByQuestionId(Long questionId);
}

