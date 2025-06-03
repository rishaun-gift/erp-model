package com.example.demo.aspect;

import com.example.demo.entity.AuditLog;
import com.example.demo.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Aspect
@Component
public class AuditTrailAspect {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    // Use ThreadLocal for thread safety
    private ThreadLocal<Object> oldEntity = new ThreadLocal<>();

    // Capture old entity before update
    @Before("execution(* com.example.demo.controller..update*(..))")
    public void captureOldData(JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg != null && isEntity(arg)) {
                try {
                    Method getIdMethod = arg.getClass().getMethod("getId");
                    Long id = (Long) getIdMethod.invoke(arg);
                    Object old = entityManager.find(arg.getClass(), id);
                    oldEntity.set(old);
                } catch (Exception e) {
                    // If error occurs, just clear the ThreadLocal
                    oldEntity.remove();
                }
            }
        }
    }

    // Audit only create, update, delete controller methods
    @AfterReturning(pointcut = "execution(* com.example.demo.controller..create*(..)) || " +
            "execution(* com.example.demo.controller..update*(..)) || " +
            "execution(* com.example.demo.controller..delete*(..))", returning = "result")
    public void logCrudAction(JoinPoint joinPoint, Object result) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return;

        HttpServletRequest request = attrs.getRequest();
        String method = request.getMethod();
        String endpoint = request.getRequestURI();
        String entityName = extractEntityName(endpoint);

        // Create audit log entry
        AuditLog log = new AuditLog();
        log.setIpAddress(request.getRemoteAddr());
        log.setEndpoint(endpoint);
        log.setMethod(method);
        log.setAction(method + " " + entityName);
        log.setEntityName(entityName);
        log.setTimestamp(OffsetDateTime.now(ZoneId.of("Asia/Kolkata")));

        try {
            // If the method is PUT or DELETE, include old data in the log
            if ("PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
                Object old = oldEntity.get();
                if (old != null) {
                    log.setOldData(objectMapper.writeValueAsString(old));
                    oldEntity.remove(); // Clear ThreadLocal after use
                }
            }

            // If the method is POST or PUT, include new data in the log
            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
                if (result != null) {
                    log.setNewData(objectMapper.writeValueAsString(result));
                }
            }

            // Save the audit log entry to the repository
            auditLogRepository.save(log);

        } catch (Exception e) {
            e.printStackTrace();  // Consider logging this instead of printing the stack trace
        }
    }

    // Utility method to check if the object is an entity
    private boolean isEntity(Object obj) {
        return obj != null && obj.getClass().isAnnotationPresent(Entity.class);
    }

    // Extract the entity name from the URL (improved handling of URI)
    private String extractEntityName(String uri) {
        String[] parts = uri.split("/");
        if (parts.length > 2) {
            String entity = parts[2];
            // Capitalize the first letter of the entity name to match naming conventions
            return capitalize(entity);
        }
        return "Unknown";
    }

    // Capitalize the first letter of the string
    private String capitalize(String str) {
        return (str == null || str.isEmpty()) ? str : str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}