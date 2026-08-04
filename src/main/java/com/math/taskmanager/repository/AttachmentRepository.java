package com.math.taskmanager.repository;

import com.math.taskmanager.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository 
        extends JpaRepository<Attachment, Long> {

	List<Attachment> findByHistoryId(
			Long historyId);
	
	List<Attachment> findByHistoryIdAndActiveTrue(
	        Long historyId
	);
	
	
	/* documentação */
	
	List<Attachment> findByHistoryTaskIdAndActiveTrueOrderByUploadedAtDesc(
	        Long taskId
	);
	
}

