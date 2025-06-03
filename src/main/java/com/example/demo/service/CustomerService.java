package com.example.demo.service;

import com.example.demo.entity.CustomerEntity;
import com.example.demo.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private HttpServletRequest request;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<CustomerEntity> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<CustomerEntity> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public CustomerEntity createCustomer(CustomerEntity customer) {
        return customerRepository.save(customer);
    }

    @Transactional
    public CustomerEntity updateCustomer(Long id, CustomerEntity customerDetails) {
        return customerRepository.findById(id).map(existingCustomer -> {
            try {
                boolean isUpdated = false;
                StringBuilder oldData = new StringBuilder();
                StringBuilder newData = new StringBuilder();

                if (!existingCustomer.getLocation().equals(customerDetails.getLocation())) {
                    isUpdated = true;
                    oldData.append("Location: ").append(existingCustomer.getLocation()).append("; ");
                    newData.append("Location: ").append(customerDetails.getLocation()).append("; ");
                }

                if (!existingCustomer.getContact().equals(customerDetails.getContact())) {
                    isUpdated = true;
                    oldData.append("Contact: ").append(existingCustomer.getContact()).append("; ");
                    newData.append("Contact: ").append(customerDetails.getContact()).append("; ");
                }

                if (!existingCustomer.getEmail().equals(customerDetails.getEmail())) {
                    isUpdated = true;
                    oldData.append("Email: ").append(existingCustomer.getEmail()).append("; ");
                    newData.append("Email: ").append(customerDetails.getEmail()).append("; ");
                }

                if (!existingCustomer.getCustomerName().equals(customerDetails.getCustomerName())) {
                    isUpdated = true;
                    oldData.append("Name: ").append(existingCustomer.getCustomerName()).append("; ");
                    newData.append("Name: ").append(customerDetails.getCustomerName()).append("; ");
                }

                if (!existingCustomer.getDate().equals(customerDetails.getDate())) {
                    isUpdated = true;
                    oldData.append("Date: ").append(existingCustomer.getDate()).append("; ");
                    newData.append("Date: ").append(customerDetails.getDate()).append("; ");
                }

                if (isUpdated) {
                    updateCustomerFields(existingCustomer, customerDetails);

                    CustomerEntity updatedCustomer = customerRepository.save(existingCustomer);

                    auditLogService.logAction(
                            request.getRemoteAddr(),
                            "Updated customer with ID " + id,
                            "/api/customer/" + id,
                            "UPDATE",
                            "Customer",
                            oldData.toString(),
                            newData.toString()
                    );

                    return updatedCustomer;
                } else {
                    return existingCustomer;
                }

            } catch (Exception e) {
                logger.error("Error updating customer with ID " + id, e);
                throw new RuntimeException("Failed to update customer", e);
            }
        }).orElse(null);
    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }

    private void updateCustomerFields(CustomerEntity existingCustomer, CustomerEntity newDetails) {
        existingCustomer.setCustomerName(newDetails.getCustomerName());
        existingCustomer.setContact(newDetails.getContact());
        existingCustomer.setEmail(newDetails.getEmail());
        existingCustomer.setDate(newDetails.getDate());
        existingCustomer.setLocation(newDetails.getLocation());
    }
}
