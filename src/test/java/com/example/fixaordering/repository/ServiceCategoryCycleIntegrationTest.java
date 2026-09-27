package com.example.fixaordering.repository;

import com.example.fixaordering.domain.ServiceCategory;
import com.example.fixaordering.exception.ServiceCategoryCycleException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ServiceCategoryCycleIntegrationTest {

    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void selfParentIsRejectedImmediately() {
        ServiceCategory root = persisted(new ServiceCategory("Root", true));

        assertThatThrownBy(() -> root.setParent(root))
                .isInstanceOf(ServiceCategoryCycleException.class);
    }

    @Test
    void closingCycleThroughReparentingIsRejected() {
        ServiceCategory a = persisted(new ServiceCategory("A", true));
        ServiceCategory b = persisted(new ServiceCategory("B", true, a));
        persisted(new ServiceCategory("C", true, b));

        assertThatThrownBy(() -> a.setParent(b))
                .isInstanceOf(ServiceCategoryCycleException.class);
        assertThatThrownBy(() -> b.setParent(b))
                .isInstanceOf(ServiceCategoryCycleException.class);
    }

    @Test
    void cycleAcrossReloadedEntitiesIsRejectedWhenMutatingFieldDirectly() throws Exception {
        ServiceCategory a = persisted(new ServiceCategory("A", true));
        ServiceCategory b = persisted(new ServiceCategory("B", true, a));
        ServiceCategory c = persisted(new ServiceCategory("C", true, b));
        Long aId = a.getId();
        Long cId = c.getId();
        entityManager.flush();
        entityManager.clear();

        ServiceCategory managedA = serviceCategoryRepository.findById(aId).orElseThrow();
        ServiceCategory managedC = serviceCategoryRepository.findById(cId).orElseThrow();

        Field parentField = ServiceCategory.class.getDeclaredField("parent");
        parentField.setAccessible(true);
        parentField.set(managedA, managedC);

        assertThatThrownBy(() -> serviceCategoryRepository.saveAndFlush(managedA))
                .isInstanceOf(ServiceCategoryCycleException.class);
    }

    @Test
    void movingSubtreeUnderAnotherRootIsAllowed() {
        ServiceCategory root1 = persisted(new ServiceCategory("Root-1", true));
        ServiceCategory root2 = persisted(new ServiceCategory("Root-2", true));
        ServiceCategory child = persisted(new ServiceCategory("Child", true, root1));
        ServiceCategory grandChild = persisted(new ServiceCategory("GrandChild", true, child));
        Long childId = child.getId();
        Long grandChildId = grandChild.getId();
        entityManager.flush();
        entityManager.clear();

        ServiceCategory managedChild = serviceCategoryRepository.findById(childId).orElseThrow();
        ServiceCategory managedRoot2 = serviceCategoryRepository.findById(root2.getId()).orElseThrow();
        managedChild.setParent(managedRoot2);
        serviceCategoryRepository.saveAndFlush(managedChild);

        ServiceCategory reloaded = serviceCategoryRepository.findById(childId).orElseThrow();
        assertThat(reloaded.getParent().getId()).isEqualTo(root2.getId());
        assertThat(serviceCategoryRepository.findById(grandChildId).orElseThrow().getParent().getId())
                .isEqualTo(childId);
    }

    private <T> T persisted(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }
}
