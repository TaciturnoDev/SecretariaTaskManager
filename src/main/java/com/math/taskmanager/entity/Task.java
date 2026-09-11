package com.math.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

/*
 * Soft delete automático
 */
@SQLDelete(sql = "UPDATE tasks SET active = false WHERE id = ?")


/*
 * Ignora registros inativos automaticamente
 */
@Where(clause = "active = true")
public class Task extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    /*
     *  Status da tarefa
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    /*
     *  Prioridade da tarefa
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority;

    /*
     * Soft delete flag
     */
    @Column(nullable = false)
    private Boolean active = true;
    
    
    /*lastMovementAt
   
     */
    
    @Column(nullable = false)
    private LocalDateTime lastMovementAt;

    /*
     * Início do ciclo da prioridade atual para o SLA.
     *
     * Controla há quanto tempo a tarefa está na prioridade atual.
     */
    @Column
    private LocalDateTime priorityChangedAt;
    
    /*
     *  Usuário que criou a tarefa
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    /*
     *  Usuário responsável pela tarefa
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_to_id", nullable = false)
    private User assignedTo;

    /*
     *  Setor da tarefa
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    /*
     *  HISTÓRICO DA TAREFA 
     */
    @OneToMany(
    	    mappedBy = "task",
    	    cascade = {
    	        CascadeType.PERSIST,
    	        CascadeType.MERGE
    	    }
    	)
    	private List<TaskHistory> history;

    @PrePersist
    public void prePersist() {

        if (this.status == null) {
            this.status = TaskStatus.PENDING;
        }

        if (this.priority == null) {
            this.priority = TaskPriority.MEDIUM;
        }

        if (this.active == null) {
            this.active = true;
        }
        
        if (lastMovementAt == null) {
        	lastMovementAt = LocalDateTime.now();
        
        }
        
        if (priorityChangedAt == null) {
            priorityChangedAt = lastMovementAt;
        }
    }
}