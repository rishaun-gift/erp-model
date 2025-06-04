package com.example.demo.controller;

import com.example.demo.entity.AuditLog;
import com.example.demo.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    @Autowired
    private AuditLogRepository auditLogRepository;


    private final String apiKey;

    public AuditLogController(Environment environment) {
        this.apiKey = environment.getProperty("auditlog.api.key");
    }


    // ✅ GET: Publicly readable
    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        List<AuditLog> logs = auditLogRepository.findAll();
        return ResponseEntity.ok(logs);
    }

    // ✅ POST: Protected by API key
    @PostMapping
    public ResponseEntity<String> logAction(@RequestBody AuditLog log,
                                            HttpServletRequest request,
                                            @RequestHeader(value = "X-API-KEY", required = false) String requestKey) {
        if (requestKey == null || !requestKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid or missing API key.");
        }

        log.setIpAddress(request.getRemoteAddr());
        log.setTimestamp(OffsetDateTime.now(ZoneId.of("Asia/Kolkata")));

        if (log.getOldData() == null) log.setOldData("");
        if (log.getNewData() == null) log.setNewData("");

        auditLogRepository.save(log);
        return ResponseEntity.ok("Audit log saved successfully.");
    }

    // ✅ DELETE ALL: Protected by API key
    @DeleteMapping("/delete-all")
    public ResponseEntity<String> deleteAllLogs(@RequestHeader(value = "X-API-KEY", required = false) String requestKey) {
        if (requestKey == null || !requestKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid or missing API key.");
        }

        auditLogRepository.deleteAll();
        return ResponseEntity.ok("All audit logs deleted.");
    }
}
