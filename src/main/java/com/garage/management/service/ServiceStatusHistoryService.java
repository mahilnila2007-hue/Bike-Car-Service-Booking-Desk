package com.garage.management.service;

import com.garage.management.model.ServiceStatusHistoryModel;
import com.garage.management.repository.ServiceStatusHistoryRepository;
import com.garage.management.util.DateTimeUtil;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Records and retrieves service status history.
 * Every status change is logged with timestamp, actor, and remarks.
 */
@Service
public class ServiceStatusHistoryService {

    private final ServiceStatusHistoryRepository historyRepository;

    public ServiceStatusHistoryService(ServiceStatusHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public void recordStatusChange(String serviceJobId, String previousStatus, String newStatus,
                                   String changedBy, String remarks)
            throws ExecutionException, InterruptedException {

        ServiceStatusHistoryModel history = new ServiceStatusHistoryModel();
        history.setHistoryId(historyRepository.generateId());
        history.setServiceJobId(serviceJobId);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setChangedAt(DateTimeUtil.now());
        history.setRemarks(remarks);

        historyRepository.save(history);
    }

    public List<ServiceStatusHistoryModel> getHistoryForJob(String serviceJobId)
            throws ExecutionException, InterruptedException {
        return historyRepository.findByServiceJobId(serviceJobId).stream()
                .sorted(Comparator.comparing(ServiceStatusHistoryModel::getChangedAt))
                .toList();
    }
}
