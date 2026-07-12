package br.com.cb.baseEntity;

import br.com.cb.uild.ULIDGenerator;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
public abstract class BaseEntity extends PanacheEntityBase {

    @Id
    @Column(nullable = false, updatable = false)
    protected UUID id;

    @Column(name = "created_at", nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    protected LocalDateTime updatedAt;

    public UUID getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @PrePersist
    protected void prePersist() {
        if (id == null) {
            id = ULIDGenerator.generate();
        }

        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}