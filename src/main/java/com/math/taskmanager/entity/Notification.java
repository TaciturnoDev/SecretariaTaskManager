package com.math.taskmanager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Usuário que receberá a notificação
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /*
     * Tipo da notificação
     */
    @Column(nullable = false, length = 50)
    private String type;

    /*
     * Título exibido para o usuário
     */
    @Column(nullable = false, length = 150)
    private String title;

    /*
     * Mensagem da notificação
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /*
     * Tipo do objeto relacionado
     * Exemplo: TASK, EVENT
     */
    @Column(length = 50)
    private String referenceType;

    /*
     * ID do objeto relacionado
     * Exemplo: ID da tarefa
     */
    private Long referenceId;

    /*
     * Indica se o usuário já leu
     */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean read = false;

    /*
     * Indica se a notificação continua visível
     */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    /*
     * Data de criação
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {

        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        if (this.read == null) {
            this.read = false;
        }

        if (this.active == null) {
            this.active = true;
        }
    }
}