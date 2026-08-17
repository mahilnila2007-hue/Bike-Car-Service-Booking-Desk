package com.garage.management.service;

import com.garage.management.exception.MechanicUnavailableException;
import com.garage.management.exception.ResourceNotFoundException;
import com.garage.management.model.MechanicModel;
import com.garage.management.repository.MechanicRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Service for mechanic management and smart assignment.
 *
 * Mechanic allocation algorithm:
 * 1. Find available (AVAILABLE status) mechanics.
 * 2. Match specialization to vehicle type.
 * 3. Prefer exact specialization.
 * 4. Fallback to GENERAL specialization.
 * 5. Assign mechanic and change status to BUSY.
 */
@Service
public class MechanicAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(MechanicAssignmentService.class);
    private final MechanicRepository mechanicRepository;

    public MechanicAssignmentService(MechanicRepository mechanicRepository) {
        this.mechanicRepository = mechanicRepository;
    }

    /**
     * Assign the best available mechanic for the given vehicle type and service job.
     */
    public MechanicModel assignMechanic(String vehicleType, String serviceJobId)
            throws ExecutionException, InterruptedException {

        List<MechanicModel> available = mechanicRepository.findByAvailabilityStatus("AVAILABLE");

        if (available.isEmpty()) {
            throw new MechanicUnavailableException(
                    "No mechanics are currently available. Please try again later.");
        }

        // Determine preferred specialization from vehicle type
        String preferredSpec = vehicleType.equals("TWO_WHEELER") ? "TWO_WHEELER" : "FOUR_WHEELER";

        // Step 1: Try exact specialization match
        Optional<MechanicModel> exactMatch = available.stream()
                .filter(m -> preferredSpec.equals(m.getSpecialization()))
                .max(Comparator.comparingInt(MechanicModel::getExperience)); // Prefer more experienced

        // Step 2: Fallback to GENERAL
        Optional<MechanicModel> generalMatch = available.stream()
                .filter(m -> "GENERAL".equals(m.getSpecialization()))
                .max(Comparator.comparingInt(MechanicModel::getExperience));

        // Step 3: Any available as last resort
        Optional<MechanicModel> anyMatch = available.stream()
                .max(Comparator.comparingInt(MechanicModel::getExperience));

        MechanicModel selected = exactMatch.orElseGet(() -> generalMatch.orElseGet(() -> anyMatch.orElse(null)));

        if (selected == null) {
            throw new MechanicUnavailableException("No suitable mechanic found for vehicle type: " + vehicleType);
        }

        // Update mechanic status to BUSY
        Map<String, Object> updates = new HashMap<>();
        updates.put("availabilityStatus", "BUSY");
        mechanicRepository.update(selected.getMechanicId(), updates);

        selected.setAvailabilityStatus("BUSY");
        log.info("Assigned mechanic {} ({}) to service job {}",
                selected.getName(), selected.getSpecialization(), serviceJobId);
        return selected;
    }

    /**
     * Manually assign a specific mechanic to a service job.
     */
    public MechanicModel assignSpecificMechanic(String mechanicId, String serviceJobId)
            throws ExecutionException, InterruptedException {
        MechanicModel mechanic = mechanicRepository.findById(mechanicId)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic not found: " + mechanicId));

        if ("BUSY".equals(mechanic.getAvailabilityStatus())) {
            throw new MechanicUnavailableException(
                    "Mechanic " + mechanic.getName() + " is currently BUSY.");
        }
        if ("ON_LEAVE".equals(mechanic.getAvailabilityStatus())) {
            throw new MechanicUnavailableException(
                    "Mechanic " + mechanic.getName() + " is currently ON_LEAVE.");
        }

        mechanicRepository.update(mechanicId, Map.of("availabilityStatus", "BUSY"));
        mechanic.setAvailabilityStatus("BUSY");
        log.info("Manually assigned mechanic {} to service job {}", mechanic.getName(), serviceJobId);
        return mechanic;
    }

    /**
     * Release a mechanic back to AVAILABLE after service job completion.
     */
    public void releaseMechanic(String mechanicId) throws ExecutionException, InterruptedException {
        mechanicRepository.update(mechanicId, Map.of("availabilityStatus", "AVAILABLE"));
        log.info("Released mechanic: {}", mechanicId);
    }

    public List<MechanicModel> getAllMechanics() throws ExecutionException, InterruptedException {
        return mechanicRepository.findAll();
    }

    public List<MechanicModel> getAvailableMechanics() throws ExecutionException, InterruptedException {
        return mechanicRepository.findByAvailabilityStatus("AVAILABLE");
    }

    public MechanicModel getMechanic(String mechanicId) throws ExecutionException, InterruptedException {
        return mechanicRepository.findById(mechanicId)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic not found: " + mechanicId));
    }

    public MechanicModel saveMechanic(MechanicModel mechanic) throws ExecutionException, InterruptedException {
        if (mechanic.getMechanicId() == null || mechanic.getMechanicId().isBlank()) {
            mechanic.setMechanicId(mechanicRepository.generateId());
        }
        mechanicRepository.save(mechanic);
        return mechanic;
    }

    public void updateMechanicStatus(String mechanicId, String status) throws ExecutionException, InterruptedException {
        mechanicRepository.update(mechanicId, Map.of("availabilityStatus", status));
    }
}
