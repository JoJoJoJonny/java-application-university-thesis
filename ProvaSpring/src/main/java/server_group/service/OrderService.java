package server_group.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server_group.model.Order;
import server_group.model.OrderState;
import server_group.model.OrderStateFactory;
import server_group.repository.OrderRepository;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStateFactory stateFactory;

    public OrderService(OrderRepository orderRepository, OrderStateFactory stateFactory) {
        this.orderRepository = orderRepository;
        this.stateFactory = stateFactory;
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public void delete(Long id) {
        orderRepository.deleteById(id);
    }

    @Transactional
    public void setInProduction(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        OrderState state = stateFactory.createState(order);
        state.startProduction(order);
        orderRepository.save(order);
    }

    @Transactional
    public void complete(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        OrderState state = stateFactory.createState(order);
        state.complete(order);
        orderRepository.save(order);
    }

    @Transactional
    public void cancel(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        OrderState state = stateFactory.createState(order);
        state.cancel(order);
        orderRepository.save(order);
    }
}
