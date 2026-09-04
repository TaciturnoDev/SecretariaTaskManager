package com.math.taskmanager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ================= TAREFA ================= */
    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    /* ================= USUÁRIO ================= */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    /* ================= DESTINATÁRIO DA DELEGAÇÃO ================= */
    
    @ManyToOne
    @JoinColumn(name = "delegated_to")
    private User delegatedTo;
    
    /* ================= PRIORIDADE dias/ SLA ================= */
    @Enumerated(EnumType.STRING)
    @Column(name = "old_priority")
    private TaskPriority oldPriority;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "new_priority")
    private TaskPriority newPriority;
    
   
    /* ================= ANEXOS ==================*/
    
    @OneToMany(
    	    mappedBy = "history",
    	    cascade = {
    	        CascadeType.PERSIST,
    	        CascadeType.MERGE
    	    }
    	)
    	private List<Attachment> attachments;

    
    /* ================= AÇÃO ================= */
    @Column(columnDefinition = "TEXT")
    private String action;
    
    /* ================= COMENTÁRIO ================= */
    
    @Column(columnDefinition = "TEXT")
    private String comment;

    /* =====================================================
       SNAPSHOT ANTIGO
    ===================================================== */

    @Column(columnDefinition = "TEXT")
    private String oldTitle;

    @Column(columnDefinition = "TEXT")
    private String oldDescription;

    /* =====================================================
       SNAPSHOT NOVO
    ===================================================== */

    @Column(columnDefinition = "TEXT")
    private String newTitle;

    @Column(columnDefinition = "TEXT")
    private String newDescription;

    /* ================= DATA ================= */
    private LocalDateTime createdAt;
}