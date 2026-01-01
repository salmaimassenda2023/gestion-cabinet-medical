package com.example.cabinetservice.client;

import com.example.cabinetservice.dto.NotificationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "http://localhost:8086")
public interface NotificationClient {

    @PostMapping("/api/notification")
    void sendNotification(@RequestBody NotificationDTO notification);
}
