package com.garage.management.service;

import com.garage.management.exception.BadRequestException;
import com.garage.management.exception.DuplicateResourceException;
import com.garage.management.exception.ForbiddenException;
import com.garage.management.exception.ResourceNotFoundException;
import com.garage.management.model.VehicleModel;
import com.garage.management.repository.VehicleRepository;
import com.garage.management.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class VehicleService {

    private static final Logger log = LoggerFactory.getLogger(VehicleService.class);
    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public VehicleModel addVehicle(String customerId, String regNumber, String vehicleType,
                                   String brand, String model, int year, String fuelType, int mileage)
            throws ExecutionException, InterruptedException {

        // Business Rule: Registration number must be unique
        if (vehicleRepository.existsByRegistrationNumber(regNumber)) {
            throw new DuplicateResourceException(
                    "Vehicle with registration number '" + regNumber + "' already exists.");
        }

        validateVehicleType(vehicleType);
        validateFuelType(fuelType);

        VehicleModel vehicle = new VehicleModel();
        vehicle.setVehicleId(vehicleRepository.generateId());
        vehicle.setCustomerId(customerId);
        vehicle.setRegistrationNumber(regNumber.toUpperCase().trim());
        vehicle.setVehicleType(vehicleType);
        vehicle.setBrand(brand);
        vehicle.setModel(model);
        vehicle.setManufacturingYear(year);
        vehicle.setFuelType(fuelType);
        vehicle.setMileage(mileage);
        vehicle.setCreatedAt(DateTimeUtil.now());
        vehicle.setUpdatedAt(DateTimeUtil.now());

        vehicleRepository.save(vehicle);
        log.info("Added vehicle {} for customer {}", regNumber, customerId);
        return vehicle;
    }

    public VehicleModel getVehicle(String vehicleId, String requesterId, boolean isStaffOrAdmin)
            throws ExecutionException, InterruptedException {
        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));

        // Customers can only see their own vehicles
        if (!isStaffOrAdmin && !vehicle.getCustomerId().equals(requesterId)) {
            throw new ForbiddenException("You can only access your own vehicles.");
        }
        return vehicle;
    }

    public List<VehicleModel> getVehiclesByCustomer(String customerId) throws ExecutionException, InterruptedException {
        return vehicleRepository.findByCustomerId(customerId);
    }

    public List<VehicleModel> getAllVehicles() throws ExecutionException, InterruptedException {
        return vehicleRepository.findAll();
    }

    public VehicleModel updateVehicle(String vehicleId, String requesterId, boolean isStaffOrAdmin,
                                       String brand, String model, int mileage)
            throws ExecutionException, InterruptedException {
        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));

        if (!isStaffOrAdmin && !vehicle.getCustomerId().equals(requesterId)) {
            throw new ForbiddenException("You can only update your own vehicles.");
        }

        Map<String, Object> updates = new HashMap<>();
        if (brand != null && !brand.isBlank()) { vehicle.setBrand(brand); updates.put("brand", brand); }
        if (model != null && !model.isBlank()) { vehicle.setModel(model); updates.put("model", model); }
        if (mileage > 0) { vehicle.setMileage(mileage); updates.put("mileage", mileage); }
        updates.put("updatedAt", DateTimeUtil.now().toString());

        vehicleRepository.update(vehicleId, updates);
        return vehicle;
    }

    public void deleteVehicle(String vehicleId, String requesterId, boolean isStaffOrAdmin)
            throws ExecutionException, InterruptedException {
        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));

        if (!isStaffOrAdmin && !vehicle.getCustomerId().equals(requesterId)) {
            throw new ForbiddenException("You can only delete your own vehicles.");
        }

        vehicleRepository.delete(vehicleId);
        log.info("Deleted vehicle: {}", vehicleId);
    }

    private void validateVehicleType(String type) {
        if (!type.equals("TWO_WHEELER") && !type.equals("FOUR_WHEELER")) {
            throw new BadRequestException("Vehicle type must be TWO_WHEELER or FOUR_WHEELER. Got: " + type);
        }
    }

    private void validateFuelType(String type) {
        List<String> valid = List.of("PETROL", "DIESEL", "ELECTRIC", "CNG", "HYBRID");
        if (!valid.contains(type)) {
            throw new BadRequestException("Fuel type must be one of: " + valid + ". Got: " + type);
        }
    }
}
