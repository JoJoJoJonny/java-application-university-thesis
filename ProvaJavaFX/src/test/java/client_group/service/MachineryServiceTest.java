package client_group.service;

import client_group.model.Machinery;
import client_group.model.Session;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MachineryServiceTest {

    private MachineryService service;
    private HttpURLConnection mockConn;

    @BeforeEach
    void setUp() {
        Session.getInstance().setToken("fake-token");
        mockConn = mock(HttpURLConnection.class);

        // Factory injection
        MachineryService.ConnectionFactory factory = (URL url) -> mockConn;
        service = new MachineryService(factory);
    }

    @Test
    void testLoadAllMachinery_success() throws Exception {
        String payload = """
            [
              {"id":1,"name":"Press A","buyDate":"2024-01-15","yearManufacture":"2020","capacity":"10t"},
              {"id":2,"name":"Lathe B","buyDate":"2023-10-01","yearManufacture":"2019","capacity":"5t"}
            ]
            """;

        when(mockConn.getResponseCode()).thenReturn(200);
        when(mockConn.getInputStream())
                .thenReturn(new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)));

        List<Machinery> list = service.loadAllMachinery();

        assertEquals(2, list.size());
        assertEquals("Press A", list.get(0).getName());
        assertEquals(LocalDate.parse("2024-01-15"), list.get(0).getBuyDate());
        verify(mockConn).disconnect();
    }

    @Test
    void testLoadAllMachinery_failure() throws Exception {
        when(mockConn.getResponseCode()).thenReturn(500);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.loadAllMachinery());
        assertTrue(ex.getMessage().contains("Failed to fetch machinery"));
    }


    @Test
    void testDeleteMachinery_success() throws Exception {
        when(mockConn.getResponseCode()).thenReturn(200);

        assertDoesNotThrow(() -> service.deleteMachinery(10));
        verify(mockConn).disconnect();
    }

    @Test
    void testDeleteMachinery_failure() throws Exception {
        when(mockConn.getResponseCode()).thenReturn(404);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.deleteMachinery(99));
        assertTrue(ex.getMessage().contains("Failed to delete machinery"));
        verify(mockConn).disconnect();
    }

    @Test
    void testSaveMachinery_success() throws Exception {
        Machinery input = new Machinery();
        input.setName("Cutter X");
        input.setBuyDate(LocalDate.parse("2024-05-20"));
        input.setYearManufacture("2024");
        input.setCapacity("12t");

        JSONObject obj = new JSONObject();
        obj.put("id", 101);
        obj.put("name", "Cutter X");
        obj.put("buyDate", "2024-05-20");
        obj.put("yearManufacture", "2024");
        obj.put("capacity", "12t");

        when(mockConn.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockConn.getResponseCode()).thenReturn(201);
        when(mockConn.getInputStream())
                .thenReturn(new ByteArrayInputStream(obj.toString().getBytes(StandardCharsets.UTF_8)));

        Optional<Machinery> saved = service.saveMachinery(input);
        assertTrue(saved.isPresent());
        assertEquals(101, saved.get().getId());
        assertEquals("Cutter X", saved.get().getName());
        verify(mockConn).disconnect();
    }

    @Test
    void testSaveMachinery_failureNon201() throws Exception {
        Machinery input = new Machinery();
        input.setName("Bad");
        input.setBuyDate(LocalDate.now());
        input.setYearManufacture("x");
        input.setCapacity("y");

        when(mockConn.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockConn.getResponseCode()).thenReturn(400);

        Optional<Machinery> saved = service.saveMachinery(input);
        assertTrue(saved.isEmpty());
        verify(mockConn).disconnect();
    }

    @Test
    void testUpdateMachinery_success() throws Exception {
        Machinery input = new Machinery();
        input.setId(7);
        input.setName("Press Z");
        input.setBuyDate(LocalDate.parse("2023-01-01"));
        input.setYearManufacture("2021");
        input.setCapacity("20t");

        JSONObject obj = new JSONObject();
        obj.put("id", 7);
        obj.put("name", "Press Z");
        obj.put("buyDate", "2023-01-01");
        obj.put("yearManufacture", "2021");
        obj.put("capacity", "20t");

        when(mockConn.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockConn.getResponseCode()).thenReturn(200);
        when(mockConn.getInputStream())
                .thenReturn(new ByteArrayInputStream(obj.toString().getBytes(StandardCharsets.UTF_8)));

        Optional<Machinery> updated = service.updateMachinery(input);
        assertTrue(updated.isPresent());
        assertEquals(7, updated.get().getId());
        assertEquals("Press Z", updated.get().getName());
        verify(mockConn).disconnect();
    }

    @Test
    void testUpdateMachinery_failureNon200() throws Exception {
        Machinery input = new Machinery();
        input.setId(8);
        input.setName("X");
        input.setBuyDate(LocalDate.now());
        input.setYearManufacture("x");
        input.setCapacity("y");

        when(mockConn.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockConn.getResponseCode()).thenReturn(500);

        Optional<Machinery> updated = service.updateMachinery(input);
        assertTrue(updated.isEmpty());
        verify(mockConn).disconnect();
    }

    @Test
    void testGetAllMachinery_success() throws Exception {
        String payload = """
            [
              {"id":3,"name":"Saw C","buyDate":"2022-02-02","yearManufacture":"2018","capacity":"3t"}
            ]
            """;

        when(mockConn.getInputStream())
                .thenReturn(new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)));

        List<Machinery> list = service.getAllMachinery();
        assertEquals(1, list.size());
        assertEquals("Saw C", list.get(0).getName());
    }

    @Test
    void testGetAllMachinery_failure_io() throws Exception {
        when(mockConn.getInputStream()).thenThrow(new IOException("boom"));
        assertThrows(IOException.class, () -> service.getAllMachinery());
    }

    @Test
    void testGetAllMachineryNames_success() throws Exception {
        String payload = """
            [
              {"id":1,"name":"A","buyDate":"2024-01-01","yearManufacture":"2020","capacity":"1"},
              {"id":2,"name":"B","buyDate":"2024-01-02","yearManufacture":"2021","capacity":"2"}
            ]
            """;
        when(mockConn.getInputStream())
                .thenReturn(new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)));

        List<String> names = service.getAllMachineryNames();
        assertEquals(List.of("A", "B"), names);
    }

    @Test
    void testGetMachineryByName_found() throws Exception {
        String payload = """
            [
              {"id":5,"name":"Target","buyDate":"2024-03-03","yearManufacture":"2022","capacity":"9"},
              {"id":6,"name":"Other","buyDate":"2024-03-04","yearManufacture":"2023","capacity":"10"}
            ]
            """;
        when(mockConn.getInputStream())
                .thenReturn(new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)));

        Machinery m = service.getMachineryByName("target");
        assertNotNull(m);
        assertEquals(5, m.getId());
        assertEquals("Target", m.getName());
    }

    @Test
    void testGetMachineryByName_notFound() throws Exception {
        String payload = """
            [
              {"id":5,"name":"Alpha","buyDate":"2024-03-03","yearManufacture":"2022","capacity":"9"}
            ]
            """;
        when(mockConn.getInputStream())
                .thenReturn(new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)));

        Machinery m = service.getMachineryByName("Nope");
        assertNull(m);
    }
}
