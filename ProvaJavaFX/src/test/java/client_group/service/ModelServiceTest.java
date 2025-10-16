package client_group.service;

import client_group.model.Model;
import client_group.model.Raw;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModelServiceTest {

    private ModelService service;
    private HttpURLConnection mockConnection;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() throws Exception {
        mapper = new ObjectMapper();

        mockConnection = mock(HttpURLConnection.class);

        // Factory injection
        service = new ModelService(url -> mockConnection);
    }

    @Test
    void testLoadAllModels_success() throws Exception {
        // Creo JSON
        String json = "[{\"name\":\"Model1\",\"price\":0,\"raw\":null,\"processSteps\":[]}," +
                "{\"name\":\"Model2\",\"price\":0,\"raw\":null,\"processSteps\":[]}]";

        ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes());
        when(mockConnection.getInputStream()).thenReturn(input);
        when(mockConnection.getResponseCode()).thenReturn(200);

        List<Model> result = service.loadAllModels();

        assertEquals(2, result.size());
        assertEquals("Model1", result.get(0).getName());
        assertEquals("Model2", result.get(1).getName());

        verify(mockConnection).setRequestMethod("GET");
    }

    @Test
    void testSaveModel_success() throws Exception {
        Model model = new Model();
        model.setName("TestModel");

        // Mock dell'OutputStream
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        when(mockConnection.getOutputStream()).thenReturn(os);

        // Mock dell'InputStream
        String responseJson = "{\"name\":\"TestModel\",\"price\":0,\"raw\":null,\"processSteps\":[]}";
        ByteArrayInputStream input = new ByteArrayInputStream(responseJson.getBytes());
        when(mockConnection.getInputStream()).thenReturn(input);

        // Mock del response code
        when(mockConnection.getResponseCode()).thenReturn(201);

        assertTrue(service.saveModel(model).isPresent());

        verify(mockConnection).setRequestMethod("POST");
        verify(mockConnection).setDoOutput(true);
        verify(mockConnection).setRequestProperty("Content-Type", "application/json");

        assertTrue(os.size() > 0);
    }


    @Test
    void testUpdateModel_success() throws Exception {
        Model model = new Model();
        model.setName("TestModel");

        // Mock OutputStream
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        when(mockConnection.getOutputStream()).thenReturn(os);

        // Mock InputStream
        ByteArrayInputStream input = new ByteArrayInputStream("{}".getBytes());
        when(mockConnection.getInputStream()).thenReturn(input);

        when(mockConnection.getResponseCode()).thenReturn(200);

        service.updateModel(model);

        verify(mockConnection).setRequestMethod("PUT");
        verify(mockConnection).setDoOutput(true);
        verify(mockConnection).setRequestProperty("Content-Type", "application/json");

        assertTrue(os.size() > 0);
    }
    @Test
    void testSaveModel_failure() throws Exception {
        Model model = new Model();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        when(mockConnection.getOutputStream()).thenReturn(os);

        when(mockConnection.getResponseCode()).thenReturn(500);

        Optional<Model> saved = service.saveModel(model);
        assertTrue(saved.isEmpty());
    }
    @Test
    void testFetchAllModels_exception() throws IOException {
        when(mockConnection.getInputStream()).thenThrow(new IOException());
        List<Model> result = service.fetchAllModels();
        assertTrue(result.isEmpty());
    }


    @Test
    void testDeleteModel_success() throws Exception {
        when(mockConnection.getResponseCode()).thenReturn(204);

        service.deleteModel("TestModel");

        verify(mockConnection).setRequestMethod("DELETE");
    }

    @Test
    void testLoadAllRaws_success() throws Exception {
        String rawJson = "[{\"id\":1,\"shape\":\"shape1\",\"material\":\"mat1\",\"size\":\"size1\"}]";
        ByteArrayInputStream input = new ByteArrayInputStream(rawJson.getBytes());

        when(mockConnection.getInputStream()).thenReturn(input);
        when(mockConnection.getResponseCode()).thenReturn(200);

        assertEquals(1, service.loadAllRaws().size());
        verify(mockConnection).setRequestMethod("GET");
    }
}
