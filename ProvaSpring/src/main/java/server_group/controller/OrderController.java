package server_group.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import server_group.model.Order;
import server_group.model.OrderStatus;
import server_group.service.OrderService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE', 'ACCOUNTANT')")
    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        List<Order> orderList = orderService.findAll();
        return ResponseEntity.ok(orderList);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE', 'ACCOUNTANT')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        Optional<Order> orderOpt = orderService.findById(id);
        if (orderOpt.isPresent()) {
            return ResponseEntity.ok(orderOpt.get());
        } else {
            return ResponseEntity.status(404).body("Order not found");
        }
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        try {
            Order created = orderService.save(order);
            return ResponseEntity.status(201).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create order: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestBody Order updatedOrder) {
        Optional<Order> existing = orderService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(404).body("Order not found");
        }

        updatedOrder.setId(id);
        Order saved = orderService.save(updatedOrder);
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        Optional<Order> existing = orderService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(404).body("Order not found");
        }

        orderService.delete(id);
        return ResponseEntity.ok("Order deleted successfully");
    }

    /*
    @PutMapping("/{id}/start")
    public ResponseEntity<?> startProduction(@PathVariable Long id) {
        Optional<Order> orderOpt = orderService.findById(id);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Order not found");
        }

        Order order = orderOpt.get();
        if (order.getStatus() != OrderStatus.CREATED) {
            return ResponseEntity.badRequest().body("Order is not in CREATED state");
        }

        order.setStatus(OrderStatus.IN_PRODUCTION);
        order.setStartDate(LocalDate.now());

        orderService.save(order);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeOrder(@PathVariable Long id) {
        Optional<Order> orderOpt = orderService.findById(id);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Order not found");
        }

        Order order = orderOpt.get();
        if (order.getStatus() != OrderStatus.IN_PRODUCTION) {
            return ResponseEntity.badRequest().body("Order is not in IN_PRODUCTION state");
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setEndDate(LocalDate.now());

        orderService.save(order);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        Optional<Order> orderOpt = orderService.findById(id);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Order not found");
        }

        Order order = orderOpt.get();
        if (order.getStatus() != OrderStatus.IN_PRODUCTION) {
            return ResponseEntity.badRequest().body("Only orders in production can be cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderService.save(order);
        return ResponseEntity.ok(order);
    }
    */

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
    @PutMapping("/{id}/in-production")
    public ResponseEntity<Void> setInProduction(@PathVariable Long id) {
        orderService.setInProduction(id); // usa lo state pattern
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
    @PutMapping("/{id}/complete")
    public ResponseEntity<Void> complete(@PathVariable Long id) {
        orderService.complete(id); // usa lo state pattern
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id); // usa lo state pattern
        return ResponseEntity.ok().build();
    }


}
