package com.garage.management.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

/**
 * Server-Sent Events service for real-time service status updates to customers.
 * Each customer can have multiple SSE connections (multiple browser tabs).
 */
@Service
public class SseService {

    private static final Logger log = LoggerFactory.getLogger(SseService.class);
    private static final long SSE_TIMEOUT = 300_000L; // 5 minutes

    // customerId → list of active SSE emitters
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * Register a new SSE connection for a customer.
     */
    public SseEmitter subscribe(String customerId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitters.computeIfAbsent(customerId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(customerId, emitter));
        emitter.onTimeout(() -> removeEmitter(customerId, emitter));
        emitter.onError(e -> removeEmitter(customerId, emitter));

        // Send initial connection confirmation
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("SSE connection established for customer: " + customerId));
        } catch (IOException e) {
            log.warn("Failed to send initial SSE event to {}: {}", customerId, e.getMessage());
        }

        log.info("New SSE subscription for customer: {}", customerId);
        return emitter;
    }

    /**
     * Send a service status update to all connections of a customer.
     */
    public void sendStatusUpdate(String customerId, String serviceJobId, String newStatus) {
        List<SseEmitter> customerEmitters = emitters.get(customerId);
        if (customerEmitters == null || customerEmitters.isEmpty()) {
            return;
        }

        String eventData = String.format(
                "{\"serviceJobId\":\"%s\",\"status\":\"%s\",\"timestamp\":\"%s\"}",
                serviceJobId, newStatus, java.time.Instant.now().toString()
        );

        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        for (SseEmitter emitter : customerEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("statusUpdate")
                        .data(eventData));
            } catch (IOException e) {
                log.warn("Failed to send SSE update to customer {}: {}", customerId, e.getMessage());
                deadEmitters.add(emitter);
            }
        }
        customerEmitters.removeAll(deadEmitters);
    }

    private void removeEmitter(String customerId, SseEmitter emitter) {
        List<SseEmitter> customerEmitters = emitters.get(customerId);
        if (customerEmitters != null) {
            customerEmitters.remove(emitter);
            if (customerEmitters.isEmpty()) {
                emitters.remove(customerId);
            }
        }
    }
}
