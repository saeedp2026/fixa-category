package com.example.fixaordering.repository;

import com.example.fixaordering.domain.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {

    @Query(value = """
            WITH RECURSIVE ancestry(id, parent_id, enabled) AS (
                SELECT id, parent_id, enabled
                FROM service_categories
                WHERE id = :categoryId
                UNION ALL
                SELECT p.id, p.parent_id, p.enabled
                FROM ancestry a
                JOIN service_categories p ON p.id = a.parent_id
            )
            SELECT COUNT(*) FROM ancestry WHERE enabled = FALSE
            """, nativeQuery = true)
    long countDisabledInAncestry(@Param("categoryId") Long categoryId);

    boolean existsByParentId(Long parentId);
}
