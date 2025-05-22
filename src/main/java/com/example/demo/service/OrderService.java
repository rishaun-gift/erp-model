package com.example.demo.service;

import com.example.demo.entity.CustomerEntity;
import com.example.demo.entity.OrderEntity;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

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
            return orderRepository.save(orders);
        } else {
            throw new RuntimeException("Customer with ID " + customerId + " not found.");
        }
    }

    public OrderEntity updateOrder(Long id, OrderEntity orderDetails, Long customerId) {
        return orderRepository.findById(id).map(orders -> {
            Optional<CustomerEntity> customerOpt = customerRepository.findById(customerId);
            if (customerOpt.isEmpty()) {
                throw new RuntimeException("Customer with ID " + customerId + " not found.");
            }

            orders.setStatus(orderDetails.getStatus());
            orders.setAmount(orderDetails.getAmount());
            orders.setCustomer(customerOpt.get());

            return orderRepository.save(orders);
        }).orElse(null);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}
