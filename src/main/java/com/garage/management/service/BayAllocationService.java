package com.garage.management.service;

import com.garage.management.exception.BayUnavailableException;
import com.garage.management.model.BayModel;
import com.garage.management.repository.BayRepository;
import com.garage.management.repository.FirestoreRepository;
import com.garage.management.repository.ServiceJobRepository;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Smart automatic bay allocation service.
 *
 * Algorithm:
 * 1. Receive vehicle type.
 * 2. Find available compatible bays.
 * 3. Ignore maintenance bays.
 * 4. Ignore occupied bays.
 * 5. Prefer exact vehicle-type bay.
 * 6. If no exact bay, use UNIVERSAL bay.
 * 7. Select lowest-numbered suitable bay.
 * 8. Assign bay using Firestore transaction.
 * 9. Update bay status to OCCUPIED.
 * 10. Store bayId in serviceJob.
 *
 * Uses Firestore transactions to prevent two simultaneous requests from receiving the same bay.
 */
@Service
public class BayAllocationService {

    private static final Logger log = LoggerFactory.getLogger(BayAllocationService.class);

    private final BayRepository bayRepository;
    private final ServiceJobRepository serviceJobRepository;
    private final FirestoreRepository firestoreRepository;

    public BayAllocationService(BayRepository bayRepository,
                                 ServiceJobRepository serviceJobRepository,
                                 FirestoreRepository firestoreRepository) {
        this.bayRepository = bayRepository;
        this.serviceJobRepository = serviceJobRepository;
        this.firestoreRepository = firestoreRepository;
    }

    /**
     * Allocate a bay for the given vehicle type and service job.
     * Uses Firestore transaction to prevent race conditions.
     *
     * @param vehicleType TWO_WHEELER or FOUR_WHEELER
     * @param serviceJobId the service job to assign the bay to
     * @return the allocated BayModel
     */
    public BayModel allocateBay(String vehicleType, String serviceJobId)
            throws ExecutionException, InterruptedException {

        Firestore db = firestoreRepository.getFirestore();
        CollectionReference baysCol = db.collection("bays");
        CollectionReference jobsCol = db.collection("serviceJobs");

        // Step 1: Get all available bays
        List<BayModel> allBays = bayRepository.findAll();

        // Step 2-4: Filter available, non-maintenance bays
        List<BayModel> availableBays = allBays.stream()
                .filter(b -> "AVAILABLE".equals(b.getStatus()))
                .filter(b -> !"MAINTENANCE".equals(b.getStatus()))
                .toList();

        if (availableBays.isEmpty()) {
            throw new BayUnavailableException(
                    "No compatible service bay is currently available. " +
                    "Please try again later or mark a bay as available.");
        }

        // Step 5-6: Prefer exact type, fallback to UNIVERSAL
        Optional<BayModel> exactBay = availableBays.stream()
                .filter(b -> vehicleType.equals(b.getBayType()))
                .min(Comparator.comparing(b -> extractBayNumber(b.getBayNumber())));

        Optional<BayModel> universalBay = availableBays.stream()
                .filter(b -> "UNIVERSAL".equals(b.getBayType()))
                .min(Comparator.comparing(b -> extractBayNumber(b.getBayNumber())));

        BayModel selectedBay = exactBay.orElseGet(() -> universalBay.orElse(null));

        if (selectedBay == null) {
            throw new BayUnavailableException(
                    "No compatible bay available for vehicle type: " + vehicleType +
                    ". All compatible bays are occupied or under maintenance.");
        }

        final String selectedBayId = selectedBay.getBayId();
        final BayModel finalBay = selectedBay;

        // Step 7-10: Use Firestore transaction to atomically assign the bay
        firestoreRepository.runTransaction(transaction -> {
            DocumentReference bayRef = baysCol.document(selectedBayId);
            DocumentReference jobRef = jobsCol.document(serviceJobId);

            DocumentSnapshot baySnapshot = transaction.get(bayRef).get();

            // Double-check bay is still available inside transaction
            String currentStatus = (String) baySnapshot.get("status");
            if (!"AVAILABLE".equals(currentStatus)) {
                throw new BayUnavailableException(
                        "Bay " + finalBay.getBayNumber() + " was just taken. Retry allocation.");
            }

            // Update bay: AVAILABLE → OCCUPIED
            transaction.update(bayRef, Map.of(
                    "status", "OCCUPIED",
                    "currentServiceJobId", serviceJobId
            ));

            // Update service job with bayId
            transaction.update(jobRef, Map.of(
                    "bayId", selectedBayId,
                    "updatedAt", java.time.Instant.now().toString()
            ));

            return null;
        });

        log.info("Allocated bay {} ({}) to service job {}",
                finalBay.getBayNumber(), finalBay.getBayType(), serviceJobId);

        finalBay.setStatus("OCCUPIED");
        finalBay.setCurrentServiceJobId(serviceJobId);
        return finalBay;
    }

    /**
     * Release a bay back to AVAILABLE when a service job completes.
     */
    public void releaseBay(String bayId) throws ExecutionException, InterruptedException {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "AVAILABLE");
        updates.put("currentServiceJobId", null);
        bayRepository.update(bayId, updates);
        log.info("Released bay: {}", bayId);
    }

    public List<BayModel> getAllBays() throws ExecutionException, InterruptedException {
        return bayRepository.findAll();
    }

    public List<BayModel> getAvailableBays() throws ExecutionException, InterruptedException {
        return bayRepository.findByStatus("AVAILABLE");
    }

    public BayModel getBay(String bayId) throws ExecutionException, InterruptedException {
        return bayRepository.findById(bayId)
                .orElseThrow(() -> new com.garage.management.exception.ResourceNotFoundException("Bay not found: " + bayId));
    }

    public BayModel saveBay(BayModel bay) throws ExecutionException, InterruptedException {
        if (bay.getBayId() == null || bay.getBayId().isBlank()) {
            bay.setBayId(bayRepository.generateId());
        }
        bayRepository.save(bay);
        return bay;
    }

    public void updateBayStatus(String bayId, String newStatus) throws ExecutionException, InterruptedException {
        bayRepository.update(bayId, Map.of("status", newStatus));
    }

    /**
     * Extract numeric bay number for sorting (e.g. "B-02" → 2).
     */
    private int extractBayNumber(String bayNumber) {
        if (bayNumber == null) return Integer.MAX_VALUE;
        try {
            String digits = bayNumber.replaceAll("[^0-9]", "");
            return digits.isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }
}
