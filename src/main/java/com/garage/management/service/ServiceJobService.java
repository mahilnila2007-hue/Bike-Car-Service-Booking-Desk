package com.garage.management.service;

import com.garage.management.exception.BadRequestException;
import com.garage.management.exception.ForbiddenException;
import com.garage.management.exception.ResourceNotFoundException;
import com.garage.management.model.BookingModel;
import com.garage.management.model.ServiceJobModel;
import com.garage.management.model.ServiceTypeModel;
import com.garage.management.model.VehicleModel;
import com.garage.management.repository.BookingRepository;
import com.garage.management.repository.ServiceJobRepository;
import com.garage.management.repository.ServiceTypeRepository;
import com.garage.management.repository.VehicleRepository;
import com.garage.management.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Service for managing service job lifecycle and status transitions.
 */
@Service
public class ServiceJobService {

    private static final Logger log = LoggerFactory.getLogger(ServiceJobService.class);

    // Valid status order
    private static final List<String> STATUS_ORDER = List.of(
            "BOOKED", "VEHICLE_RECEIVED", "BAY_ALLOCATED", "SERVICE_STARTED",
            "SERVICE_IN_PROGRESS", "WAITING_FOR_PARTS", "QUALITY_CHECK",
            "READY_FOR_DELIVERY", "COMPLETED"
    );

    private final ServiceJobRepository serviceJobRepository;
    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final BayAllocationService bayAllocationService;
    private final MechanicAssignmentService mechanicAssignmentService;
    private final ServiceStatusHistoryService statusHistoryService;
    private final SseService sseService;

    public ServiceJobService(ServiceJobRepository serviceJobRepository,
                              BookingRepository bookingRepository,
                              VehicleRepository vehicleRepository,
                              ServiceTypeRepository serviceTypeRepository,
                              BayAllocationService bayAllocationService,
                              MechanicAssignmentService mechanicAssignmentService,
                              ServiceStatusHistoryService statusHistoryService,
                              SseService sseService) {
        this.serviceJobRepository = serviceJobRepository;
        this.bookingRepository = bookingRepository;
        this.vehicleRepository = vehicleRepository;
        this.serviceTypeRepository = serviceTypeRepository;
        this.bayAllocationService = bayAllocationService;
        this.mechanicAssignmentService = mechanicAssignmentService;
        this.statusHistoryService = statusHistoryService;
        this.sseService = sseService;
    }

    /**
     * Create a service job from a confirmed booking.
     */
    public ServiceJobModel createServiceJob(String bookingId, String staffUid)
            throws ExecutionException, InterruptedException {

        BookingModel booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        // Check if service job already exists for this booking
        serviceJobRepository.findByBookingId(bookingId).ifPresent(existing -> {
            throw new BadRequestException("Service job already exists for booking: " + bookingId);
        });

        ServiceJobModel job = new ServiceJobModel();
        job.setServiceJobId(serviceJobRepository.generateId());
        job.setBookingId(bookingId);
        job.setVehicleId(booking.getVehicleId());
        job.setCustomerId(booking.getCustomerId());
        job.setCurrentStatus("VEHICLE_RECEIVED");
        job.setCreatedAt(DateTimeUtil.now());
        job.setUpdatedAt(DateTimeUtil.now());

        serviceJobRepository.save(job);

        statusHistoryService.recordStatusChange(
                job.getServiceJobId(), null, "VEHICLE_RECEIVED", staffUid, "Service job created.");

        log.info("Created service job {} for booking {}", job.getServiceJobId(), bookingId);
        return job;
    }

    /**
     * Automatically allocate a bay to the service job.
     */
    public ServiceJobModel allocateBay(String serviceJobId, String staffUid)
            throws ExecutionException, InterruptedException {

        ServiceJobModel job = getJobById(serviceJobId);
        VehicleModel vehicle = vehicleRepository.findById(job.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + job.getVehicleId()));

        var bay = bayAllocationService.allocateBay(vehicle.getVehicleType(), serviceJobId);

        job.setBayId(bay.getBayId());
        job.setCurrentStatus("BAY_ALLOCATED");
        job.setUpdatedAt(DateTimeUtil.now());

        Map<String, Object> updates = new HashMap<>();
        updates.put("bayId", bay.getBayId());
        updates.put("currentStatus", "BAY_ALLOCATED");
        updates.put("updatedAt", DateTimeUtil.now().toString());
        serviceJobRepository.update(serviceJobId, updates);

        statusHistoryService.recordStatusChange(serviceJobId,
                "VEHICLE_RECEIVED", "BAY_ALLOCATED", staffUid,
                "Bay " + bay.getBayNumber() + " allocated.");

        sseService.sendStatusUpdate(job.getCustomerId(), serviceJobId, "BAY_ALLOCATED");
        log.info("Bay {} allocated to service job {}", bay.getBayNumber(), serviceJobId);
        return job;
    }

    /**
     * Assign a mechanic (automatic or manual) to the service job.
     */
    public ServiceJobModel assignMechanic(String serviceJobId, String mechanicId, String staffUid)
            throws ExecutionException, InterruptedException {

        ServiceJobModel job = getJobById(serviceJobId);
        VehicleModel vehicle = vehicleRepository.findById(job.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + job.getVehicleId()));

        var mechanic = (mechanicId != null && !mechanicId.isBlank())
                ? mechanicAssignmentService.assignSpecificMechanic(mechanicId, serviceJobId)
                : mechanicAssignmentService.assignMechanic(vehicle.getVehicleType(), serviceJobId);

        job.setMechanicId(mechanic.getMechanicId());
        job.setUpdatedAt(DateTimeUtil.now());

        Map<String, Object> updates = new HashMap<>();
        updates.put("mechanicId", mechanic.getMechanicId());
        updates.put("updatedAt", DateTimeUtil.now().toString());
        serviceJobRepository.update(serviceJobId, updates);

        statusHistoryService.recordStatusChange(serviceJobId,
                job.getCurrentStatus(), job.getCurrentStatus(), staffUid,
                "Mechanic " + mechanic.getName() + " assigned.");

        return job;
    }

    /**
     * Start the service — sets status to SERVICE_STARTED and calculates ETA.
     */
    public ServiceJobModel startService(String serviceJobId, String staffUid)
            throws ExecutionException, InterruptedException {

        ServiceJobModel job = getJobById(serviceJobId);
        BookingModel booking = bookingRepository.findById(job.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));

        // Calculate ETA from service type duration
        int durationMinutes = 120; // default 2 hours
        List<ServiceTypeModel> types = serviceTypeRepository.findAll();
        for (ServiceTypeModel st : types) {
            if (st.getName().equalsIgnoreCase(booking.getServiceType())) {
                durationMinutes = st.getEstimatedDurationMinutes();
                break;
            }
        }

        Instant now = DateTimeUtil.now();
        Instant eta = DateTimeUtil.calculateEta(now, durationMinutes);

        String prevStatus = job.getCurrentStatus();
        job.setStartTime(now);
        job.setEstimatedCompletionTime(eta);
        job.setCurrentStatus("SERVICE_STARTED");
        job.setUpdatedAt(now);

        Map<String, Object> updates = new HashMap<>();
        updates.put("startTime", now.toString());
        updates.put("estimatedCompletionTime", eta.toString());
        updates.put("currentStatus", "SERVICE_STARTED");
        updates.put("updatedAt", now.toString());
        serviceJobRepository.update(serviceJobId, updates);

        statusHistoryService.recordStatusChange(serviceJobId, prevStatus, "SERVICE_STARTED",
                staffUid, "Service started. ETA: " + DateTimeUtil.format(eta));

        sseService.sendStatusUpdate(job.getCustomerId(), serviceJobId, "SERVICE_STARTED");
        return job;
    }

    /**
     * Update service job status with history recording and SSE notification.
     */
    public ServiceJobModel updateStatus(String serviceJobId, String newStatus, String remarks, String staffUid)
            throws ExecutionException, InterruptedException {

        ServiceJobModel job = getJobById(serviceJobId);
        String prevStatus = job.getCurrentStatus();

        if ("COMPLETED".equals(prevStatus)) {
            throw new BadRequestException("Service job is already completed. Cannot update status.");
        }

        if (!STATUS_ORDER.contains(newStatus)) {
            throw new BadRequestException("Invalid service status: " + newStatus);
        }

        job.setCurrentStatus(newStatus);
        job.setUpdatedAt(DateTimeUtil.now());

        Map<String, Object> updates = new HashMap<>();
        updates.put("currentStatus", newStatus);
        updates.put("updatedAt", DateTimeUtil.now().toString());

        // If completing: release bay and mechanic, record completion time
        if ("COMPLETED".equals(newStatus)) {
            Instant now = DateTimeUtil.now();
            updates.put("actualCompletionTime", now.toString());
            job.setActualCompletionTime(now);

            if (job.getBayId() != null) {
                bayAllocationService.releaseBay(job.getBayId());
            }
            if (job.getMechanicId() != null) {
                mechanicAssignmentService.releaseMechanic(job.getMechanicId());
            }

            // Update booking status to COMPLETED
            bookingRepository.update(job.getBookingId(), Map.of(
                    "status", "COMPLETED",
                    "updatedAt", DateTimeUtil.now().toString()
            ));
        }

        serviceJobRepository.update(serviceJobId, updates);
        statusHistoryService.recordStatusChange(serviceJobId, prevStatus, newStatus, staffUid, remarks);
        sseService.sendStatusUpdate(job.getCustomerId(), serviceJobId, newStatus);

        log.info("Updated service job {} status: {} → {}", serviceJobId, prevStatus, newStatus);
        return job;
    }

    /**
     * Update service notes.
     */
    public void updateNotes(String serviceJobId, String notes) throws ExecutionException, InterruptedException {
        serviceJobRepository.update(serviceJobId, Map.of(
                "serviceNotes", notes,
                "updatedAt", DateTimeUtil.now().toString()
        ));
    }

    /**
     * Update ETA (staff can adjust for delays).
     */
    public ServiceJobModel updateEta(String serviceJobId, String newEtaIso, String staffUid)
            throws ExecutionException, InterruptedException {
        ServiceJobModel job = getJobById(serviceJobId);
        Instant eta = Instant.parse(newEtaIso);
        serviceJobRepository.update(serviceJobId, Map.of(
                "estimatedCompletionTime", eta.toString(),
                "updatedAt", DateTimeUtil.now().toString()
        ));
        job.setEstimatedCompletionTime(eta);
        statusHistoryService.recordStatusChange(serviceJobId, job.getCurrentStatus(),
                job.getCurrentStatus(), staffUid, "ETA updated to: " + DateTimeUtil.format(eta));
        return job;
    }

    public ServiceJobModel getJobById(String serviceJobId) throws ExecutionException, InterruptedException {
        return serviceJobRepository.findById(serviceJobId)
                .orElseThrow(() -> new ResourceNotFoundException("Service job not found: " + serviceJobId));
    }

    public ServiceJobModel getJobByBookingId(String bookingId) throws ExecutionException, InterruptedException {
        return serviceJobRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("No service job for booking: " + bookingId));
    }

    public List<ServiceJobModel> getAllJobs() throws ExecutionException, InterruptedException {
        return serviceJobRepository.findAll();
    }

    public List<ServiceJobModel> getJobsByCustomer(String customerId) throws ExecutionException, InterruptedException {
        return serviceJobRepository.findByCustomerId(customerId);
    }

    public List<ServiceJobModel> getJobsByStatus(String status) throws ExecutionException, InterruptedException {
        return serviceJobRepository.findByStatus(status);
    }
}
