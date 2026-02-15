package com.mindterminal.backend.repository;

import com.mindterminal.backend.model.MindNode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MindRepository extends JpaRepository<MindNode, Long> {
    Optional<MindNode> findByLabelIgnoreCase(String label);
    List<MindNode> findByParentId(Long parentId);
}