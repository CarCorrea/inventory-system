package com.challenge.meli.inventory.config;

import com.challenge.meli.inventory.service.InventoryService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulingConfig {

    private final InventoryService inventoryService;

    public SchedulingConfig(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Scheduled(fixedRate = 300000)
    public void cleanupExpiredReservations(){
        inventoryService.cleanupExpiredReservation();
    }
}
