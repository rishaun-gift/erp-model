package com.example.demo.controller;

import com.example.demo.entity.AuditLog;
import com.example.demo.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    @Autowired
    private AuditLogRepository auditLogRepository;

    // GET: Fetch all audit logs
    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        List<AuditLog> logs = auditLogRepository.findAll();
        return ResponseEntity.ok(logs); // Always returns 200 OK with [] if empty
    }

    // POST: Save a new audit log
    @PostMapping
    public ResponseEntity<String> logAction(@RequestBody AuditLog log, HttpServletRequest request) {
        log.setIpAddress(request.getRemoteAddr());
        log.setTimestamp(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));
        auditLogRepository.save(log);
        return ResponseEntity.ok("Audit log saved successfully.");
    }
}
