package client_group.service;

import client_group.model.Raw;
import client_group.model.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RawServiceTest {

    private HttpURLConnection mockConnection;
    private Supplier<HttpURLConnection> connectionSupplier;
    private RawService service;

    @BeforeEach
    void setUp() {
        mockConnection = mock(HttpURLConnection.class);
        connectionSupplier = () -> mockConnection;
        service = new RawService(connectionSupplier);
        Session.getInstance().setToken("fake-token");
    }

    @Test
    void testFetchAllRaw_success() throws Exception {
        JSONArray arr = new JSONArray();
        JSONObject obj = new JSONObject();
        obj.put("id", 1L);
        obj.put("shape", "circle");
        obj.put("material", "steel");
        obj.put("size", "10x10");
        obj.put("castingNumber", "C123");
        obj.put("thickness", "5mm");
        arr.put(obj);

        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getInputStream())
                .thenReturn(new ByteArrayInputStream(arr.toString().getBytes(StandardCharsets.UTF_8)));

        List<Raw> raws = service.fetchAllRaw();
        assertEquals(1, raws.size());
        assertEquals("steel", raws.get(0).getMaterial());
        verify(mockConnection).disconnect();
    }

    @Test
    void testFetchAllRaw_failure() throws Exception {
        when(mockConnection.getResponseCode()).thenReturn(500);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.fetchAllRaw());
        assertTrue(ex.getMessage().contains("Failed to fetch raw materials"));
    }

    @Test
    void testFetchAllRaw_exception() throws Exception {
        when(mockConnection.getResponseCode()).thenThrow(new IOException("Network error"));
        assertThrows(Exception.class, () -> service.fetchAllRaw());
    }

    @Test
    void testDeleteRaw_success() throws Exception {
        when(mockConnection.getResponseCode()).thenReturn(200);
        assertDoesNotThrow(() -> service.deleteRaw(10L));
        verify(mockConnection).disconnect();
    }

    @Test
    void testDeleteRaw_failure() throws Exception {
        when(mockConnection.getResponseCode()).thenReturn(404);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.deleteRaw(99L));
        assertTrue(ex.getMessage().contains("Failed to delete raw"));
    }

    @Test
    void testDeleteRaw_exception() throws Exception {
        when(mockConnection.getResponseCode()).thenThrow(new IOException("boom"));
        assertThrows(Exception.class, () -> service.deleteRaw(1L));
    }

    @Test
    void testCreateRaw_success() throws Exception {
        Raw input = new Raw();
        input.setShape("square");
        input.setMaterial("iron");
        input.setSize("5x5");
        input.setCastingNumber("CN1");
        input.setThickness("2mm");

        JSONObject obj = new JSONObject();
        obj.put("id", 100);
        obj.put("shape", "square");
        obj.put("material", "iron");
        obj.put("size", "5x5");
        obj.put("castingNumber", "CN1");
        obj.put("thickness", "2mm");

        when(mockConnection.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockConnection.getResponseCode()).thenReturn(201);
        when(mockConnection.getInputStream())
                .thenReturn(new ByteArrayInputStream(obj.toString().getBytes(StandardCharsets.UTF_8)));

        Raw created = service.createRaw(input);
        assertEquals(100L, created.getId());
        assertEquals("iron", created.getMaterial());
        verify(mockConnection).disconnect();
    }

    @Test
    void testCreateRaw_failure() throws Exception {
        Raw input = new Raw();
        input.setShape("bad");

        when(mockConnection.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockConnection.getResponseCode()).thenReturn(400);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.createRaw(input));
        assertTrue(ex.getMessage().contains("Failed to create raw"));
    }

    @Test
    void testCreateRaw_exception() throws Exception {
        Raw input = new Raw();
        input.setShape("bad");
        when(mockConnection.getOutputStream()).thenThrow(new IOException("broken"));
        assertThrows(Exception.class, () -> service.createRaw(input));
    }

    @Test
    void testUpdateRaw_success() throws Exception {
        Raw input = new Raw();
        input.setId(1L);
        input.setShape("hex");
        input.setMaterial("bronze");
        input.setSize("20x20");
        input.setCastingNumber("C999");
        input.setThickness("3mm");

        JSONObject obj = new JSONObject();
        obj.put("id", 1);
        obj.put("shape", "hex");
        obj.put("material", "bronze");
        obj.put("size", "20x20");
        obj.put("castingNumber", "C999");
        obj.put("thickness", "3mm");

        when(mockConnection.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getInputStream())
                .thenReturn(new ByteArrayInputStream(obj.toString().getBytes(StandardCharsets.UTF_8)));

        Raw updated = service.updateRaw(input);
        assertEquals("bronze", updated.getMaterial());
        verify(mockConnection).disconnect();
    }

    @Test
    void testUpdateRaw_failure() throws Exception {
        Raw input = new Raw();
        input.setId(99L);

        when(mockConnection.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockConnection.getResponseCode()).thenReturn(500);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.updateRaw(input));
        assertTrue(ex.getMessage().contains("Failed to update raw"));
    }

    @Test
    void testUpdateRaw_exception() throws Exception {
        Raw input = new Raw();
        input.setId(1L);
        when(mockConnection.getOutputStream()).thenThrow(new IOException("bad IO"));
        assertThrows(Exception.class, () -> service.updateRaw(input));
    }

    @Test
    void testLoadAllRaw_success() throws Exception {
        JSONArray arr = new JSONArray();
        JSONObject obj = new JSONObject();
        obj.put("id", 7);
        obj.put("shape", "oval");
        obj.put("material", "aluminum");
        obj.put("size", "30x40");
        obj.put("castingNumber", "A7");
        obj.put("thickness", "7mm");
        arr.put(obj);

        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getInputStream())
                .thenReturn(new ByteArrayInputStream(arr.toString().getBytes(StandardCharsets.UTF_8)));

        List<Raw> raws = service.loadAllRaw();
        assertEquals(1, raws.size());
        assertEquals("aluminum", raws.get(0).getMaterial());
        verify(mockConnection).disconnect();
    }

    @Test
    void testLoadAllRaw_failure() throws Exception {
        when(mockConnection.getResponseCode()).thenReturn(404);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.loadAllRaw());
        assertTrue(ex.getMessage().contains("Failed to fetch raw data"));
    }

    @Test
    void testGetAllRaws_success() throws Exception {
        Raw raw = new Raw();
        raw.setId(42L);
        raw.setShape("triangle");
        raw.setMaterial("copper");
        raw.setSize("15x15");
        raw.setCastingNumber("TRI42");
        raw.setThickness("1mm");

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(List.of(raw));

        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getInputStream())
                .thenReturn(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));

        List<Raw> raws = service.getAllRaws();
        assertEquals(1, raws.size());
        assertEquals("triangle", raws.get(0).getShape());
    }

    @Test
    void testGetAllRaws_failure() throws Exception {
        when(mockConnection.getResponseCode()).thenReturn(500);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getAllRaws());
        assertTrue(ex.getMessage().contains("Failed to fetch raw data"));
    }
}
