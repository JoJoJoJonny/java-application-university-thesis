package client_group.service;

import client_group.model.*;
import client_group.model.Order;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private OrderService service;
    private static HttpURLConnection mockConn;

    @BeforeEach
    void setUp() {
        // factory injection
        service = new OrderService(() -> mockConn);
        Session.getInstance().setToken("fake-token");
    }

    @Test
    void testFetchAll_success() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("id", 1);
        obj.put("quantity", 10);
        obj.put("status", OrderStatus.CREATED.name());
        obj.put("createDate", LocalDate.now().toString());

        JSONObject model = new JSONObject();
        model.put("id", 2);
        model.put("name", "ModelX");
        obj.put("model", model);

        JSONObject client = new JSONObject();
        client.put("piva", "123");
        client.put("companyName", "ACME");
        obj.put("client", client);

        String jsonArray = "[" + obj.toString() + "]";

        mockConn = mock(HttpURLConnection.class);
        when(mockConn.getResponseCode()).thenReturn(200);
        when(mockConn.getInputStream())
                .thenReturn(new ByteArrayInputStream(jsonArray.getBytes(StandardCharsets.UTF_8)));

        List<Order> orders = service.fetchAll();
        assertEquals(1, orders.size());
        assertEquals("ACME", orders.get(0).getClient().getCompanyName());
        verify(mockConn).disconnect();
    }

    @Test
    void testFetchAll_failure() throws Exception {
        mockConn = mock(HttpURLConnection.class);
        when(mockConn.getResponseCode()).thenReturn(500);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.fetchAll());
        assertTrue(ex.getMessage().contains("Failed to fetch orders"));
    }

    @Test
    void testCreateOrder_success() throws Exception {
        Order input = makeDummyOrder();

        JSONObject obj = new JSONObject();
        obj.put("id", 99);
        obj.put("quantity", 5);
        obj.put("status", OrderStatus.IN_PRODUCTION.name());

        JSONObject client = new JSONObject();
        client.put("piva", "777");
        client.put("companyName", "ClientY");
        obj.put("client", client);

        JSONObject model = new JSONObject();
        model.put("id", 10);
        model.put("name", "ModelY");
        obj.put("model", model);

        mockConn = mock(HttpURLConnection.class);
        when(mockConn.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockConn.getResponseCode()).thenReturn(201);
        when(mockConn.getInputStream())
                .thenReturn(new ByteArrayInputStream(obj.toString().getBytes(StandardCharsets.UTF_8)));

        Order created = service.createOrder(input);
        assertEquals(99L, created.getId());
        assertEquals(OrderStatus.IN_PRODUCTION, created.getStatus());
        verify(mockConn).disconnect();
    }

    @Test
    void testCreateOrder_failure() throws Exception {
        Order input = makeDummyOrder();

        mockConn = mock(HttpURLConnection.class);
        when(mockConn.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockConn.getResponseCode()).thenReturn(400);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.createOrder(input));
        assertTrue(ex.getMessage().contains("Failed to create order"));
    }

    @Test
    void testUpdateOrder_success() throws Exception {
        Order input = makeDummyOrder();
        input.setId(123L);

        JSONObject obj = new JSONObject();
        obj.put("id", 123);
        obj.put("quantity", 42);
        obj.put("status", OrderStatus.COMPLETED.name());

        JSONObject client = new JSONObject();
        client.put("piva", "456");
        client.put("companyName", "UpdatedClient");
        obj.put("client", client);

        JSONObject model = new JSONObject();
        model.put("id", 12);
        model.put("name", "UpdatedModel");
        obj.put("model", model);

        mockConn = mock(HttpURLConnection.class);
        when(mockConn.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockConn.getResponseCode()).thenReturn(200);
        when(mockConn.getInputStream())
                .thenReturn(new ByteArrayInputStream(obj.toString().getBytes(StandardCharsets.UTF_8)));

        Order updated = service.updateOrder(input);
        assertEquals(123L, updated.getId());
        assertEquals(OrderStatus.COMPLETED, updated.getStatus());
        assertEquals("UpdatedClient", updated.getClient().getCompanyName());
        verify(mockConn).disconnect();
    }

    @Test
    void testUpdateOrder_failure() throws Exception {
        Order input = makeDummyOrder();
        input.setId(888L);

        mockConn = mock(HttpURLConnection.class);
        when(mockConn.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockConn.getResponseCode()).thenReturn(500);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.updateOrder(input));
        assertTrue(ex.getMessage().contains("Failed to update order"));
    }

    @Test
    void testUpdateOrderState_success() throws Exception {
        mockConn = mock(HttpURLConnection.class);
        when(mockConn.getResponseCode()).thenReturn(200);

        assertDoesNotThrow(() -> service.updateOrderState(1L, "start"));
    }

    @Test
    void testUpdateOrderState_failure() throws Exception {
        mockConn = mock(HttpURLConnection.class);
        when(mockConn.getResponseCode()).thenReturn(404);

        IOException ex = assertThrows(IOException.class, () -> service.updateOrderState(1L, "fail"));
        assertTrue(ex.getMessage().contains("Errore dal server"));
    }

    private Order makeDummyOrder() {
        Order order = new Order();
        order.setQuantity(5);
        order.setStatus(OrderStatus.CREATED);
        order.setCreateDate(LocalDate.now());

        Model model = new Model();
        Raw raw = new Raw();
        raw.setId(1L);
        model.setRaw(raw);
        model.setName("ModelX");
        order.setModel(model);

        Client client = new Client();
        client.setPiva("123");
        client.setCompanyName("ACME");
        order.setClient(client);

        return order;
    }
}
