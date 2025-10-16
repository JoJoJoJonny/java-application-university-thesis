package server_group.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import server_group.model.Order;
import server_group.model.OrderStatus;
import server_group.service.OrderService;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    }

    @Test
    void testGetAllOrders() throws Exception {
        Order order1 = new Order();
        order1.setId(1L);
        Order order2 = new Order();
        order2.setId(2L);

        when(orderService.findAll()).thenReturn(Arrays.asList(order1, order2));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(orderService, times(1)).findAll();
    }

    @Test
    void testGetOrderById_Found() throws Exception {
        Order order = new Order();
        order.setId(1L);

        when(orderService.findById(1L)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(orderService, times(1)).findById(1L);
    }

    @Test
    void testGetOrderById_NotFound() throws Exception {
        when(orderService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Order not found"));

        verify(orderService, times(1)).findById(1L);
    }

    @Test
    void testCreateOrder_Success() throws Exception {
        Order order = new Order();
        order.setId(1L);

        when(orderService.save(any(Order.class))).thenReturn(order);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(orderService, times(1)).save(any(Order.class));
    }

    @Test
    void testCreateOrder_Failure() throws Exception {
        Order order = new Order();
        when(orderService.save(any(Order.class))).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Failed to create order: DB error"));

        verify(orderService, times(1)).save(any(Order.class));
    }

    @Test
    void testUpdateOrder_Found() throws Exception {
        Order existingOrder = new Order();
        existingOrder.setId(1L);

        Order updatedOrder = new Order();
        updatedOrder.setId(1L);

        when(orderService.findById(1L)).thenReturn(Optional.of(existingOrder));
        when(orderService.save(any(Order.class))).thenReturn(updatedOrder);

        mockMvc.perform(put("/api/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedOrder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(orderService, times(1)).findById(1L);
        verify(orderService, times(1)).save(any(Order.class));
    }

    @Test
    void testUpdateOrder_NotFound() throws Exception {
        when(orderService.findById(1L)).thenReturn(Optional.empty());

        Order updatedOrder = new Order();

        mockMvc.perform(put("/api/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedOrder)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Order not found"));

        verify(orderService, times(1)).findById(1L);
        verify(orderService, never()).save(any(Order.class));
    }

    @Test
    void testDeleteOrder_Found() throws Exception {
        Order order = new Order();
        order.setId(1L);

        when(orderService.findById(1L)).thenReturn(Optional.of(order));

        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order deleted successfully"));

        verify(orderService, times(1)).findById(1L);
        verify(orderService, times(1)).delete(1L);
    }

    @Test
    void testDeleteOrder_NotFound() throws Exception {
        when(orderService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Order not found"));

        verify(orderService, times(1)).findById(1L);
        verify(orderService, never()).delete(anyLong());
    }

    @Test
    void testSetInProduction() throws Exception {
        mockMvc.perform(put("/api/orders/1/in-production"))
                .andExpect(status().isOk());

        verify(orderService, times(1)).setInProduction(1L);
    }

    @Test
    void testComplete() throws Exception {
        mockMvc.perform(put("/api/orders/1/complete"))
                .andExpect(status().isOk());

        verify(orderService, times(1)).complete(1L);
    }

    @Test
    void testCancel() throws Exception {
        mockMvc.perform(put("/api/orders/1/cancel"))
                .andExpect(status().isOk());

        verify(orderService, times(1)).cancel(1L);
    }
}
