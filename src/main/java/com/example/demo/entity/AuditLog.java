package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Data
@Table(name = "audit_log")
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ipAddress;
    private String action;
    private String endpoint;
    private String method; // e.g., CREATE, READ, UPDATE, DELETE, CLICK
    private String entityName;

    @Column(name = "timestamp")
    private ZonedDateTime timestamp;
}
