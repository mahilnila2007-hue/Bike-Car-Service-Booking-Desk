package com.garage.management.service;

import com.garage.management.model.*;
import com.garage.management.repository.*;
import com.garage.management.util.DateTimeUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Seeds demo data into Firestore for demonstration purposes.
 * Only callable by admins. Do NOT use in production with real credentials.
 */
@Service
public class SeedDataService {

    private static final Logger log = LoggerFactory.getLogger(SeedDataService.class);

    private final BayRepository bayRepository;
    private final MechanicRepository mechanicRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final PartRepository partRepository;

    public SeedDataService(BayRepository bayRepository,
                           MechanicRepository mechanicRepository,
                           ServiceTypeRepository serviceTypeRepository,
                           PartRepository partRepository) {
        this.bayRepository = bayRepository;
        this.mechanicRepository = mechanicRepository;
        this.serviceTypeRepository = serviceTypeRepository;
        this.partRepository = partRepository;
    }

    public Map<String, Object> seedAll() throws ExecutionException, InterruptedException {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("bays", seedBays());
        result.put("mechanics", seedMechanics());
        result.put("serviceTypes", seedServiceTypes());
        result.put("parts", seedParts());
        log.info("Demo data seeded successfully.");
        return result;
    }

    private int seedBays() throws ExecutionException, InterruptedException {
        List<String[]> baysData = List.of(
                new String[]{"B-01", "TWO_WHEELER"},
                new String[]{"B-02", "TWO_WHEELER"},
                new String[]{"B-03", "FOUR_WHEELER"},
                new String[]{"B-04", "FOUR_WHEELER"},
                new String[]{"B-05", "UNIVERSAL"},
                new String[]{"B-06", "UNIVERSAL"}
        );

        int count = 0;
        for (String[] bd : baysData) {
            BayModel bay = new BayModel();
            bay.setBayId(bayRepository.generateId());
            bay.setBayNumber(bd[0]);
            bay.setBayType(bd[1]);
            bay.setStatus("AVAILABLE");
            bayRepository.save(bay);
            count++;
        }
        log.info("Seeded {} bays", count);
        return count;
    }

    private int seedMechanics() throws ExecutionException, InterruptedException {
        List<Object[]> mechanicsData = List.of(
                new Object[]{"Kumar Selvam", "9876543210", "TWO_WHEELER", 8},
                new Object[]{"Ravi Shankar", "9765432109", "FOUR_WHEELER", 12},
                new Object[]{"Suresh Kumar", "9654321098", "ENGINE", 10},
                new Object[]{"Anand Raj", "9543210987", "ELECTRICAL", 6},
                new Object[]{"Murugan V", "9432109876", "GENERAL", 4}
        );

        int count = 0;
        for (Object[] md : mechanicsData) {
            MechanicModel mechanic = new MechanicModel();
            mechanic.setMechanicId(mechanicRepository.generateId());
            mechanic.setName((String) md[0]);
            mechanic.setPhone((String) md[1]);
            mechanic.setSpecialization((String) md[2]);
            mechanic.setExperience((int) md[3]);
            mechanic.setAvailabilityStatus("AVAILABLE");
            mechanicRepository.save(mechanic);
            count++;
        }
        log.info("Seeded {} mechanics", count);
        return count;
    }

    private int seedServiceTypes() throws ExecutionException, InterruptedException {
        List<Object[]> typesData = List.of(
                new Object[]{"General Service", "Complete general service including oil change and filter", 120, 500.0},
                new Object[]{"Oil Change", "Engine oil and filter replacement", 45, 300.0},
                new Object[]{"Brake Service", "Brake pad inspection and replacement", 90, 600.0},
                new Object[]{"Engine Service", "Engine tuning and overhaul", 240, 2000.0},
                new Object[]{"Tyre Service", "Tyre rotation, balancing and replacement", 60, 400.0},
                new Object[]{"Battery Service", "Battery check, charging and replacement", 30, 200.0},
                new Object[]{"AC Service", "Air conditioning gas refill and cleaning", 120, 800.0},
                new Object[]{"Periodic Service", "Manufacturer-recommended periodic maintenance", 150, 700.0},
                new Object[]{"Repair", "General repairs as needed", 180, 1000.0},
                new Object[]{"Custom Service", "Custom service as requested", 60, 500.0}
        );

        int count = 0;
        for (Object[] td : typesData) {
            ServiceTypeModel st = new ServiceTypeModel();
            st.setServiceTypeId(serviceTypeRepository.generateId());
            st.setName((String) td[0]);
            st.setDescription((String) td[1]);
            st.setEstimatedDurationMinutes((int) td[2]);
            st.setDefaultLabourCost((double) td[3]);
            st.setActive(true);
            serviceTypeRepository.save(st);
            count++;
        }
        log.info("Seeded {} service types", count);
        return count;
    }

    private int seedParts() throws ExecutionException, InterruptedException {
        List<Object[]> partsData = List.of(
                new Object[]{"Engine Oil 1L", "OIL-001", 350.0, 50},
                new Object[]{"Oil Filter", "FLT-001", 120.0, 30},
                new Object[]{"Air Filter", "FLT-002", 180.0, 25},
                new Object[]{"Brake Pad Set", "BRK-001", 850.0, 15},
                new Object[]{"Spark Plug", "IGN-001", 95.0, 40},
                new Object[]{"Coolant 1L", "COL-001", 220.0, 20},
                new Object[]{"Wiper Blade", "WIP-001", 280.0, 20},
                new Object[]{"Battery 35Ah", "BAT-001", 3500.0, 8},
                new Object[]{"Tyre 90/90-10", "TYR-001", 1800.0, 12},
                new Object[]{"Chain Kit", "CHN-001", 650.0, 10}
        );

        int count = 0;
        for (Object[] pd : partsData) {
            PartModel part = new PartModel();
            part.setPartId(partRepository.generateId());
            part.setPartName((String) pd[0]);
            part.setPartNumber((String) pd[1]);
            part.setPrice((double) pd[2]);
            part.setStockQuantity((int) pd[3]);
            part.setActive(true);
            partRepository.save(part);
            count++;
        }
        log.info("Seeded {} parts", count);
        return count;
    }
}
