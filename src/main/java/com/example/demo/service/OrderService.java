package com.example.demo.service;

import com.example.demo.entity.CustomerEntity;
import com.example.demo.entity.OrderEntity;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.OrderRepository;
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
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private HttpServletRequest request;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<OrderEntity> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<OrderEntity> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public OrderEntity createOrder(OrderEntity orders, Long customerId) {
        Optional<CustomerEntity> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isPresent()) {
            orders.setCustomer(customerOpt.get());
            OrderEntity savedOrder = orderRepository.save(orders);

            String newData = formatOrderData(savedOrder);

            auditLogService.logAction(
                    request.getRemoteAddr(),
                    "Created order with ID " + savedOrder.getOrderId(),
                    "/api/order",
                    "POST",
                    "Order",
                    "",
                    newData
            );

            return savedOrder;
        } else {
            throw new RuntimeException("Customer with ID " + customerId + " not found.");
        }
    }


    @Transactional
    public OrderEntity updateOrder(Long id, OrderEntity orderDetails, Long customerId) {
        return orderRepository.findById(id).map(existingOrder -> {
            try {
                Optional<CustomerEntity> customerOpt = customerRepository.findById(customerId);
                if (customerOpt.isEmpty()) {
                    throw new RuntimeException("Customer with ID " + customerId + " not found.");
                }

                // Format old data
                String oldData = formatOrderData(existingOrder);

                // Update fields
                existingOrder.setStatus(orderDetails.getStatus());
                existingOrder.setAmount(orderDetails.getAmount());
                existingOrder.setProduct(orderDetails.getProduct());
                existingOrder.setQuantity(orderDetails.getQuantity());
                existingOrder.setCustomer(customerOpt.get()); // important!

                OrderEntity updatedOrder = orderRepository.save(existingOrder);

                // Format new data
                String newData = formatOrderData(updatedOrder);

                auditLogService.logAction(
                        request.getRemoteAddr(),
                        "Updated order with ID " + id,
                        "/api/order/" + id,
                        "PUT",
                        "Order",
                        oldData,
                        newData
                );

                return updatedOrder;

            } catch (Exception e) {
                logger.error("Error updating order with ID " + id, e);
                throw new RuntimeException("Failed to update order", e);
            }
        }).orElse(null);
    }


    public void deleteOrder(Long id) {
        Optional<OrderEntity> existingOpt = orderRepository.findById(id);
        if (existingOpt.isPresent()) {
            OrderEntity existing = existingOpt.get();

            orderRepository.deleteById(id);

            String oldData = formatOrderData(existing);

            auditLogService.logAction(
                    request.getRemoteAddr(),
                    "Deleted order with ID " + id,
                    "/api/order/" + id,
                    "DELETE",
                    "Order",
                    oldData,
                    ""
            );
        }
    }


    private String formatOrderData(OrderEntity order) {
        return "id: " + order.getOrderId() + "\n"
                + "status: " + order.getStatus() + "\n"
                + "amount: " + order.getAmount() + "\n"
                + "quantity: " + order.getQuantity() + "\n"
                + "product: " + order.getProduct() + "\n"
                + "customerId: " + (order.getCustomer() != null ? order.getCustomer().getId() : "null") + "\n"
                + "customerName: " + (order.getCustomer() != null ? order.getCustomer().getCustomerName() : "null");
    }


}
