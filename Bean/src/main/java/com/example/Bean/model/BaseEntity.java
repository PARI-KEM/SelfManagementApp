package com.example.Bean.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    protected long id;

    @CreationTimestamp
    @Column(nullable = false,updatable = false)
    protected OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    protected OffsetDateTime updatedAt;


    public void setCompleted(boolean b) {
    }
}
