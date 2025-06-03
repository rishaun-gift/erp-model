package com.example.demo.controller;

import com.example.demo.entity.CustomerEntity;
import com.example.demo.service.CustomerService;
import com.example.demo.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/customer")
// @CrossOrigin(origins = "https://localhost:3000")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public List<CustomerEntity> getAllCustomers() {
        return customerService.getAllCustomers(); // No logging for view
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<CustomerEntity>> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.of(Optional.ofNullable(customerService.getCustomerById(id))); // No logging for view
    }

    @PostMapping
    public CustomerEntity createCustomer(@RequestBody CustomerEntity customer, HttpServletRequest request) {
        CustomerEntity saved = customerService.createCustomer(customer);
        return saved;
    }

    @PutMapping("/{id}")
    public CustomerEntity updateCustomer(@PathVariable Long id, @RequestBody CustomerEntity customerDetails, HttpServletRequest request) {
        Optional<CustomerEntity> existing = customerService.getCustomerById(id);
        CustomerEntity updated = customerService.updateCustomer(id, customerDetails);
        return updated;
    }

    @DeleteMapping("/{id}")
    public void deleteCustomer(@PathVariable Long id, HttpServletRequest request) {
        Optional<CustomerEntity> existing = customerService.getCustomerById(id);
        customerService.deleteCustomer(id);
    }
}
