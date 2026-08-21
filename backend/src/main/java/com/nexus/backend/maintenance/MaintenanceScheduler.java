package com.nexus.backend.maintenance;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceScheduler {

    private final MaintenanceService maintenanceService;

    public MaintenanceScheduler(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @Scheduled(cron = "${nexus.maintenance.cron}")
    public void runScheduledCleanup() {
        maintenanceService.cleanupExpired();
    }
}
