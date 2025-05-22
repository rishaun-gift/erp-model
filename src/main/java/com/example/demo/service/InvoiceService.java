package com.example.demo.service;

import com.example.demo.entity.CustomerEntity;
import com.example.demo.entity.InvoiceEntity;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CustomerRepository customerRepository;

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
            return invoiceRepository.save(invoice);
        } else {
            throw new RuntimeException("Customer with ID " + customerId + " not found.");
        }
    }

    public InvoiceEntity updateInvoice(Long id, InvoiceEntity invoiceDetails, Long customerId) {
        return invoiceRepository.findById(id).map(invoice -> {
            Optional<CustomerEntity> customerOpt = customerRepository.findById(customerId);
            if (customerOpt.isEmpty()) {
                throw new RuntimeException("Customer with ID " + customerId + " not found.");
            }

            invoice.setDueMonth(invoiceDetails.getDueMonth());
            invoice.setAmount(invoiceDetails.getAmount());
            invoice.setCustomer(customerOpt.get());

            return invoiceRepository.save(invoice);
        }).orElse(null);
    }

    public void deleteInvoice(Long id) {
        invoiceRepository.deleteById(id);
    }
}
