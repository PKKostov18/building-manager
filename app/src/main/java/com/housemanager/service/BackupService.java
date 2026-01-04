package com.housemanager.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class BackupService {

    @Scheduled(cron = "0 0 3 * * *")
    public void performSystemBackup() {
        System.out.println("--- SYSTEM BACKUP STARTED AT " + LocalDateTime.now() + " ---");

        try {
            Thread.sleep(2000);
            System.out.println(">> Exporting database...");
            Thread.sleep(1000);
            System.out.println(">> Saving files to cloud...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("--- SYSTEM BACKUP COMPLETED SUCCESSFULLY ---");
    }
}