package com.example.demo.service;

import com.example.demo.entity.CustomerEntity;
import com.example.demo.entity.InvoiceEntity;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.InvoiceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InvoiceService {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private HttpServletRequest request;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<InvoiceEntity> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Optional<InvoiceEntity> getInvoiceById(Long id) {
        return invoiceRepository.findById(id);
    }

    public InvoiceEntity createInvoice(InvoiceEntity invoice, Long customerId) {
        Optional<CustomerEntity> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isPresent()) {
            invoice.setCustomer(customerOpt.get());
            InvoiceEntity savedInvoice = invoiceRepository.save(invoice);

            String newData = formatInvoiceData(savedInvoice);

            auditLogService.logAction(
                    request.getRemoteAddr(),
                    "Created invoice with ID " + savedInvoice.getInvoiceId(),
                    "/api/invoice",
                    "POST",
                    "Invoice",
                    "",
                    newData
            );

            return savedInvoice;
        } else {
            throw new RuntimeException("Customer with ID " + customerId + " not found.");
        }
    }

    @Transactional
    public InvoiceEntity updateInvoice(Long id, InvoiceEntity invoiceDetails, Long customerId) {
        return invoiceRepository.findById(id).map(existingInvoice -> {
            try {
                Optional<CustomerEntity> customerOpt = customerRepository.findById(customerId);
                if (customerOpt.isEmpty()) {
                    throw new RuntimeException("Customer with ID " + customerId + " not found.");
                }

                String oldData = formatInvoiceData(existingInvoice);

                existingInvoice.setDueMonth(invoiceDetails.getDueMonth());
                existingInvoice.setAmount(invoiceDetails.getAmount());
                existingInvoice.setSupplierName(invoiceDetails.getSupplierName());
                existingInvoice.setDescription(invoiceDetails.getDescription());
                existingInvoice.setCustomer(customerOpt.get());

                InvoiceEntity updatedInvoice = invoiceRepository.save(existingInvoice);

                String newData = formatInvoiceData(updatedInvoice);

                auditLogService.logAction(
                        request.getRemoteAddr(),
                        "Updated invoice with ID " + id,
                        "/api/invoice/" + id,
                        "PUT",
                        "Invoice",
                        oldData,
                        newData
                );

                return updatedInvoice;

            } catch (Exception e) {
                logger.error("Error updating invoice with ID " + id, e);
                throw new RuntimeException("Failed to update invoice", e);
            }
        }).orElse(null);
    }


    public void deleteInvoice(Long id) {
        Optional<InvoiceEntity> existingOpt = invoiceRepository.findById(id);
        if (existingOpt.isPresent()) {
            InvoiceEntity existing = existingOpt.get();

            invoiceRepository.deleteById(id);

            String oldData = formatInvoiceData(existing);

            auditLogService.logAction(
                    request.getRemoteAddr(),
                    "Deleted invoice with ID " + id,
                    "/api/invoice/" + id,
                    "DELETE",
                    "Invoice",
                    oldData,
                    ""
            );
        }
    }

    private String formatInvoiceData(InvoiceEntity invoice) {
        return "id: " + invoice.getInvoiceId() + "\n"
                + "dueMonth: " + invoice.getDueMonth() + "\n"
                + "amount: " + invoice.getAmount() + "\n"
                + "supplierName: " + invoice.getSupplierName() + "\n"
                + "description: " + invoice.getDescription() + "\n"
                + "customerId: " + (invoice.getCustomer() != null ? invoice.getCustomer().getId() : "null") + "\n"
                + "customerName: " + (invoice.getCustomer() != null ? invoice.getCustomer().getCustomerName() : "null");
    }
}
