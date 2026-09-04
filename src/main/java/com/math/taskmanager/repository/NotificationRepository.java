package com.math.taskmanager.repository;

import com.math.taskmanager.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdAndActiveTrueOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndReadFalseAndActiveTrue(Long userId);
}