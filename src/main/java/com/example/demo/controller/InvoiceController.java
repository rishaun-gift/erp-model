package com.example.demo.controller;

import com.example.demo.entity.InvoiceEntity;
import com.example.demo.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping
    public List<InvoiceEntity> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }

    @GetMapping("/{id}")
    public Optional<InvoiceEntity> getInvoiceById(@PathVariable Long id) {
        return invoiceService.getInvoiceById(id);
    }

    @PostMapping
    public InvoiceEntity createInvoice(@RequestBody InvoiceEntity invoice,
                                       @RequestParam Long customerId) {
        return invoiceService.createInvoice(invoice, customerId);
    }

    @PutMapping("/{id}")
    public InvoiceEntity updateInvoice(@PathVariable Long id,
                                       @RequestBody InvoiceEntity invoiceDetails,
                                       @RequestParam Long customerId) {
        return invoiceService.updateInvoice(id, invoiceDetails, customerId);
    }

    @DeleteMapping("/{id}")
    public void deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
    }
}
