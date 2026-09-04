package com.math.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToMany(mappedBy = "assignedTo", cascade = CascadeType.ALL)
    private List<Task> assignedTasks;
    
    @JsonIgnore
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<Task> createdTasks;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {

        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        if (this.active == null) {
            this.active = true;
        }

        if (this.role == null) {
            this.role = Role.USER;
        }
    }

    //  ESSENCIAL PRO TASKSERVICE
    public boolean isSuperAdmin() {
        return Role.SUPERADMIN.equals(this.role);
    }
}