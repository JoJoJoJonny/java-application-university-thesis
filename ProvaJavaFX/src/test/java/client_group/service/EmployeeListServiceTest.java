package client_group.service;

import client_group.model.Employee;
import org.junit.jupiter.api.*;
import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;

class EmployeeListServiceTest {

    private HttpURLConnection mockConnection;
    private Supplier<HttpURLConnection> connectionSupplier;
    private EmployeeListService service;

    @BeforeEach
    void setUp() {
        mockConnection = mock(HttpURLConnection.class);
        connectionSupplier = () -> mockConnection;
        service = new EmployeeListService(connectionSupplier);
    }

    @Test
    void testFetchEmployeesSuccess() throws Exception {
        String fakeJson = "[{\"email\":\"e1@test.it\",\"name\":\"Mario\",\"surname\":\"Rossi\",\"phone\":\"111\"}," +
                "{\"email\":\"e2@test.it\",\"name\":\"Luigi\",\"surname\":\"Bianchi\",\"phone\":\"222\"}]";

        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream(fakeJson.getBytes()));

        List<Employee> result = service.fetchEmployees();

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("Mario", result.get(0).getName());
        Assertions.assertEquals("Bianchi", result.get(1).getSurname());
    }

    @Test
    void testFetchEmployeesHttpError() throws Exception {
        when(mockConnection.getResponseCode()).thenReturn(500);

        List<Employee> result = service.fetchEmployees();

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void testFetchEmployeesExceptionHandled() throws Exception {
        // Simula eccezione al getResponseCode
        when(mockConnection.getResponseCode()).thenThrow(new RuntimeException("boom"));

        List<Employee> result = service.fetchEmployees();

        Assertions.assertTrue(result.isEmpty());
    }
}
