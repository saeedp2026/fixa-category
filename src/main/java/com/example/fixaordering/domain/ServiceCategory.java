package com.example.fixaordering.domain;

import com.example.fixaordering.exception.ServiceCategoryCycleException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

@Entity
@Table(name = "service_categories")
public class ServiceCategory extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private boolean enabled;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ServiceCategory parent;

    protected ServiceCategory() {
    }

    public ServiceCategory(String name, boolean enabled) {
        this(name, enabled, null);
    }

    public ServiceCategory(String name, boolean enabled, ServiceCategory parent) {
        this.name = name;
        this.enabled = enabled;
        setParent(parent);
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ServiceCategory getParent() {
        return parent;
    }

    public void setParent(ServiceCategory parent) {
        this.parent = parent;
        assertNoCycle();
    }

    @PrePersist
    @PreUpdate
    private void assertNoCycleBeforeSave() {
        assertNoCycle();
    }

    private void assertNoCycle() {
        Set<ServiceCategory> transientVisited = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<Long> visitedIds = new HashSet<>();
        Long selfId = getId();
        ServiceCategory current = getParent();
        while (current != null) {
            if (current == this) {
                throw new ServiceCategoryCycleException(getId(), getName());
            }
            Long currentId = current.getId();
            if (currentId != null) {
                if ((selfId != null && currentId.equals(selfId)) || !visitedIds.add(currentId)) {
                    throw new ServiceCategoryCycleException(getId(), getName());
                }
            } else if (!transientVisited.add(current)) {
                throw new ServiceCategoryCycleException(getId(), getName());
            }
            current = current.getParent();
        }
    }
}
