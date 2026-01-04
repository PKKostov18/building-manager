package com.housemanager.repository;

import com.housemanager.model.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {
    long countByTimestampBetween(LocalDateTime start, LocalDateTime end);
}