package com.example.demo.service;

import com.example.demo.entity.AuditLog;
import com.example.demo.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void logAction(String ipAddress, String action, String endpoint, String method,
                          String entity, String oldData, String newData) {
        AuditLog log = new AuditLog();
        log.setIpAddress(ipAddress);
        log.setAction(action);
        log.setEndpoint(endpoint);
        log.setMethod(method);
        log.setEntityName(entity);
        log.setTimestamp(OffsetDateTime.now());
        log.setOldData(oldData);
        log.setNewData(newData);
        auditLogRepository.save(log);
    }

    public void deleteAllLogs() {
        auditLogRepository.deleteAll();
    }
}
