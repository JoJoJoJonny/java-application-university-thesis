package server_group.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server_group.model.Order;
import server_group.model.OrderState;
import server_group.model.OrderStateFactory;
import server_group.repository.OrderRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderStateFactory stateFactory;

    @Mock
    private OrderState orderState;  // mock dello stato

    @InjectMocks
    private OrderService orderService;

    private Order order;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        order = new Order();
        order.setId(1L);
    }

    @Test
    void findAll_success() {
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<Order> result = orderService.findAll();

        assertEquals(1, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    void findById_success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Optional<Order> result = orderService.findById(1L);

        assertTrue(result.isPresent());
        verify(orderRepository).findById(1L);
    }

    @Test
    void save_success() {
        when(orderRepository.save(order)).thenReturn(order);

        Order saved = orderService.save(order);

        assertEquals(order, saved);
        verify(orderRepository).save(order);
    }

    @Test
    void delete_success() {
        orderService.delete(1L);

        verify(orderRepository).deleteById(1L);
    }

    // --- setInProduction ---
    @Test
    void setInProduction_success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(stateFactory.createState(order)).thenReturn(orderState);

        orderService.setInProduction(1L);

        verify(orderState).startProduction(order);
        verify(orderRepository).save(order);
    }

    @Test
    void setInProduction_orderNotFound_throws() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> orderService.setInProduction(1L));
    }

    // --- complete ---
    @Test
    void complete_success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(stateFactory.createState(order)).thenReturn(orderState);

        orderService.complete(1L);

        verify(orderState).complete(order);
        verify(orderRepository).save(order);
    }

    @Test
    void complete_orderNotFound_throws() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> orderService.complete(1L));
    }

    // --- cancel ---
    @Test
    void cancel_success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(stateFactory.createState(order)).thenReturn(orderState);

        orderService.cancel(1L);

        verify(orderState).cancel(order);
        verify(orderRepository).save(order);
    }

    @Test
    void cancel_orderNotFound_throws() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> orderService.cancel(1L));
    }
}
