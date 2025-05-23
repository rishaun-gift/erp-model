package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;


@Entity
@Data
@Table(name = "customer")
@NoArgsConstructor
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //@JsonProperty("customerId")
    private Long id;

    @JsonProperty("name")
    private String customerName;
    private String contact;
    private String email;
    private String date;

}
