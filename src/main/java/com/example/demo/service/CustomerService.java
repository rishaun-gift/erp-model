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
        try {
            CustomerEntity savedCustomer = customerRepository.save(customer);

            // Format the new customer data in the desired style
            String newData = formatCustomerData(savedCustomer);

            auditLogService.logAction(
                    request.getRemoteAddr(),
                    "Created customer with ID " + savedCustomer.getId(),
                    "/api/customer",
                    "POST",
                    "Customer",
                    "",
                    newData
            );

            return savedCustomer;

        } catch (Exception e) {
            logger.error("Error creating customer", e);
            throw new RuntimeException("Failed to create customer", e);
        }
    }


    @Transactional
    public CustomerEntity updateCustomer(Long id, CustomerEntity customerDetails) {
        return customerRepository.findById(id).map(existingCustomer -> {
            try {
                // Full old data before update
                String oldData = objectMapper.writeValueAsString(existingCustomer);

                // Update fields
                updateCustomerFields(existingCustomer, customerDetails);
                CustomerEntity updatedCustomer = customerRepository.save(existingCustomer);

                // Full new data after update
                String newData = objectMapper.writeValueAsString(updatedCustomer);

                // Audit log
                auditLogService.logAction(
                        request.getRemoteAddr(),
                        "Updated customer with ID " + id,
                        "/api/customer/" + id,
                        "PUT",
                        "Customer",
                        oldData,
                        newData
                );

                return updatedCustomer;

            } catch (Exception e) {
                logger.error("Error updating customer with ID " + id, e);
                throw new RuntimeException("Failed to update customer", e);
            }
        }).orElse(null);
    }

    public void deleteCustomer(Long id) {
        Optional<CustomerEntity> optionalCustomer = customerRepository.findById(id);
        if (optionalCustomer.isEmpty()) {
            throw new RuntimeException("Customer with ID " + id + " not found.");
        }

        CustomerEntity customer = optionalCustomer.get();
        String oldData = formatCustomerData(customer);

        customerRepository.deleteById(id);

        auditLogService.logAction(
                request.getRemoteAddr(),
                "Deleted customer with ID " + id,
                "/api/customer/" + id,
                "DELETE",
                "Customer",
                oldData,
                ""
        );
    }


    private void updateCustomerFields(CustomerEntity existingCustomer, CustomerEntity newDetails) {
        existingCustomer.setCustomerName(newDetails.getCustomerName());
        existingCustomer.setContact(newDetails.getContact());
        existingCustomer.setEmail(newDetails.getEmail());
        existingCustomer.setDate(newDetails.getDate());
        existingCustomer.setLocation(newDetails.getLocation());
    }

    private String formatCustomerData(CustomerEntity customer) {
        return "id: " + customer.getId() + "\n"
                + "contact: " + customer.getContact() + "\n"
                + "email: " + customer.getEmail() + "\n"
                + "date: " + customer.getDate() + "\n"
                + "location: " + customer.getLocation() + "\n"
                + "name: " + customer.getCustomerName();
    }

}
