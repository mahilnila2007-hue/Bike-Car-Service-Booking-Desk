package com.garage.management.service;

import com.garage.management.model.ServiceTypeModel;
import com.garage.management.repository.ServiceTypeRepository;
import com.garage.management.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ServiceTypeService {

    private final ServiceTypeRepository serviceTypeRepository;

    public ServiceTypeService(ServiceTypeRepository serviceTypeRepository) {
        this.serviceTypeRepository = serviceTypeRepository;
    }

    public ServiceTypeModel create(String name, String description, int durationMinutes, double labourCost)
            throws ExecutionException, InterruptedException {
        ServiceTypeModel st = new ServiceTypeModel();
        st.setServiceTypeId(serviceTypeRepository.generateId());
        st.setName(name);
        st.setDescription(description);
        st.setEstimatedDurationMinutes(durationMinutes);
        st.setDefaultLabourCost(labourCost);
        st.setActive(true);
        serviceTypeRepository.save(st);
        return st;
    }

    public ServiceTypeModel getById(String id) throws ExecutionException, InterruptedException {
        return serviceTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service type not found: " + id));
    }

    public List<ServiceTypeModel> getAll() throws ExecutionException, InterruptedException {
        return serviceTypeRepository.findAll();
    }

    public List<ServiceTypeModel> getActive() throws ExecutionException, InterruptedException {
        return serviceTypeRepository.findActive();
    }

    public ServiceTypeModel update(String id, String name, String desc, int duration, double cost, boolean active)
            throws ExecutionException, InterruptedException {
        ServiceTypeModel st = getById(id);
        if (name != null) st.setName(name);
        if (desc != null) st.setDescription(desc);
        if (duration > 0) st.setEstimatedDurationMinutes(duration);
        if (cost >= 0) st.setDefaultLabourCost(cost);
        st.setActive(active);
        serviceTypeRepository.save(st);
        return st;
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        serviceTypeRepository.delete(id);
    }
}
