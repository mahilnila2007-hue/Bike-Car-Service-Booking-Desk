package com.garage.management.controller;

import com.garage.management.security.FirebaseUserDetails;
import com.garage.management.service.SseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
@Tag(name = "Server-Sent Events", description = "Real-time service status updates via SSE")
public class SseController {

    private final SseService sseService;

    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    @Operation(summary = "Subscribe to real-time service status updates for the current customer")
    public SseEmitter subscribe(@AuthenticationPrincipal FirebaseUserDetails userDetails) {
        return sseService.subscribe(userDetails.getUid());
    }
}
