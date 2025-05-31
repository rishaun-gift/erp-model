package com.example.demo.aspect;

import com.example.demo.entity.AuditLog;
import com.example.demo.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Aspect
@Component
public class AuditTrailAspect {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @AfterReturning("execution(* com.yourpackage.controller..*(..))") // Covers all controller methods
    public void logCrudAction(JoinPoint joinPoint) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return;

        HttpServletRequest request = attrs.getRequest();

        String method = request.getMethod(); // GET, POST, PUT, DELETE
        String endpoint = request.getRequestURI();
        String entityName = extractEntityName(endpoint);

        AuditLog log = new AuditLog();
        log.setIpAddress(request.getRemoteAddr());
        log.setEndpoint(endpoint);
        log.setMethod(method);
        log.setAction(method + " " + entityName);
        log.setEntityName(entityName);
        log.setTimestamp(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));

        auditLogRepository.save(log);
    }

    private String extractEntityName(String uri) {
        // Assumes /api/{entity}/... — change this logic if your URLs differ
        String[] parts = uri.split("/");
        return parts.length > 2 ? capitalize(parts[2]) : "Unknown";
    }

    private String capitalize(String str) {
        return (str == null || str.isEmpty()) ? str : str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
