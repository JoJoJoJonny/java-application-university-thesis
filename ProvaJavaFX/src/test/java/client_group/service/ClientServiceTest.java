package client_group.service;

import client_group.model.Client;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;

class ClientServiceTest {

    private ObjectMapper mapper;
    private HttpURLConnection mockConnection;
    private Supplier<HttpURLConnection> connectionSupplier;
    private ClientService service;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mockConnection = mock(HttpURLConnection.class);

        // factory injection
        connectionSupplier = () -> mockConnection;
        service = new ClientService(connectionSupplier);
    }

    @Test
    void testFetchAllClientsSuccess() throws Exception {
        List<Client> expected = List.of(
                new Client("123", "Mario SRL", "info@mario.it", "111222333")
        );

        byte[] json = mapper.writeValueAsBytes(expected);
        ByteArrayInputStream in = new ByteArrayInputStream(json);

        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getInputStream()).thenReturn(in);

        List<Client> result = service.fetchAllClients();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("123", result.get(0).getPiva());
        Assertions.assertEquals("Mario SRL", result.get(0).getCompanyName());
    }

    @Test
    void testFetchAllClientsErrorCode() throws Exception {
        when(mockConnection.getResponseCode()).thenReturn(500);

        List<Client> result = service.fetchAllClients();

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void testDeleteClientSuccess204() throws Exception {
        when(mockConnection.getResponseCode()).thenReturn(204);

        Assertions.assertDoesNotThrow(() -> service.deleteClient("123"));
        verify(mockConnection).disconnect();
    }

    @Test
    void testDeleteClientFailure() throws Exception {
        when(mockConnection.getResponseCode()).thenReturn(400);

        Assertions.assertThrows(RuntimeException.class,
                () -> service.deleteClient("123"));
    }

    @Test
    void testCreateClientSuccess() throws Exception {
        Client input = new Client("123", "Mario SRL", "info@mario.it", "111222333");
        Client expected = new Client("123", "Mario SRL", "info@mario.it", "111222333");

        byte[] json = mapper.writeValueAsBytes(expected);
        ByteArrayInputStream in = new ByteArrayInputStream(json);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(mockConnection.getOutputStream()).thenReturn(out);
        when(mockConnection.getResponseCode()).thenReturn(201);
        when(mockConnection.getInputStream()).thenReturn(in);

        Client result = service.createClient(input);

        Assertions.assertEquals("123", result.getPiva());
        Assertions.assertEquals("Mario SRL", result.getCompanyName());
    }

    @Test
    void testCreateClientError() throws Exception {
        Client input = new Client("123", "Mario SRL", "info@mario.it", "111222333");

        when(mockConnection.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockConnection.getResponseCode()).thenReturn(500);

        Assertions.assertThrows(RuntimeException.class,
                () -> service.createClient(input));
    }

    @Test
    void testUpdateClientSuccess() throws Exception {
        Client input = new Client("123", "Mario SRL", "info@mario.it", "111222333");
        Client expected = new Client("123", "Mario SRL Updated", "new@mario.it", "999888777");

        byte[] json = mapper.writeValueAsBytes(expected);
        ByteArrayInputStream in = new ByteArrayInputStream(json);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(mockConnection.getOutputStream()).thenReturn(out);
        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getInputStream()).thenReturn(in);

        Client result = service.updateClient(input);

        Assertions.assertEquals("123", result.getPiva());
        Assertions.assertEquals("Mario SRL Updated", result.getCompanyName());
        Assertions.assertEquals("new@mario.it", result.getEmail());
    }

    @Test
    void testUpdateClientError() throws Exception {
        Client input = new Client("123", "Mario SRL", "info@mario.it", "111222333");

        when(mockConnection.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockConnection.getResponseCode()).thenReturn(500);

        Assertions.assertThrows(RuntimeException.class,
                () -> service.updateClient(input));
    }
}
