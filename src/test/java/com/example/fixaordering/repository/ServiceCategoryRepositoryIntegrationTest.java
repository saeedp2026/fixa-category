package com.example.fixaordering.repository;

import com.example.fixaordering.domain.ServiceCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ServiceCategoryRepositoryIntegrationTest {

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void enabledChainHasNoDisabledAncestorOrSelf() {
        ServiceCategory root = persisted(new ServiceCategory("Root", true));
        ServiceCategory mid = persisted(new ServiceCategory("Mid", true, root));
        ServiceCategory leaf = persisted(new ServiceCategory("Leaf", true, mid));

        assertThat(serviceCategoryRepository.countDisabledInAncestry(leaf.getId())).isZero();
    }

    @Test
    void disabledMiddleAncestorIsDetectedThroughRecursion() {
        ServiceCategory root = persisted(new ServiceCategory("Root", true));
        ServiceCategory mid = persisted(new ServiceCategory("Mid", false, root));
        ServiceCategory leaf = persisted(new ServiceCategory("Leaf", true, mid));

        assertThat(serviceCategoryRepository.countDisabledInAncestry(leaf.getId())).isEqualTo(1);
        assertThat(serviceCategoryRepository.countDisabledInAncestry(mid.getId())).isEqualTo(1);
        assertThat(serviceCategoryRepository.countDisabledInAncestry(root.getId())).isZero();
    }

    @Test
    void disabledCategoryItselfIsCountedEvenWithEnabledParents() {
        ServiceCategory root = persisted(new ServiceCategory("Root", true));
        ServiceCategory leaf = persisted(new ServiceCategory("Leaf", false, root));

        assertThat(serviceCategoryRepository.countDisabledInAncestry(leaf.getId())).isEqualTo(1);
        assertThat(serviceCategoryRepository.countDisabledInAncestry(root.getId())).isZero();
    }

    @Test
    void deepChainIsWalkedAllTheWayToTheRoot() {
        ServiceCategory current = persisted(new ServiceCategory("Level-0", false));
        for (int level = 1; level <= 10; level++) {
            current = persisted(new ServiceCategory("Level-" + level, true, current));
        }

        assertThat(serviceCategoryRepository.countDisabledInAncestry(current.getId())).isEqualTo(1);
    }

    @Test
    void rootWithoutParentIsCheckedDirectly() {
        ServiceCategory root = persisted(new ServiceCategory("Root", true));

        assertThat(serviceCategoryRepository.countDisabledInAncestry(root.getId())).isZero();
    }

    private <T> T persisted(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }
}
