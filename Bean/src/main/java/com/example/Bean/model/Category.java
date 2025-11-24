package com.example.Bean.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;



@Entity
@Table(name = "categories")
@Getter
@Setter
public class Category extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String colorHex;

    @Column(nullable = false)
    private Boolean isDeleted = false;
}
