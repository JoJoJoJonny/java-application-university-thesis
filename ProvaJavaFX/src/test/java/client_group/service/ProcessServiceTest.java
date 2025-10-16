package client_group.service;

import client_group.dto.ModelWithStepsDTO;
import client_group.model.Machinery;
import client_group.model.ProcessStep;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;

class ProcessServiceTest {

    private ObjectMapper mapper;
    private HttpURLConnection mockConnection;
    private Supplier<HttpURLConnection> connectionSupplier;
    private ProcessService service;
    private MachineryService mockMachineryService;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mockConnection = mock(HttpURLConnection.class);

        // Factory injection
        connectionSupplier = () -> mockConnection;

        // Mock di MachineryService
        mockMachineryService = mock(MachineryService.class);

        service = new ProcessService(mockMachineryService, connectionSupplier);
    }
    @Test
    void testDeleteByIdSuccess() throws Exception {
        when(mockConnection.getResponseCode()).thenReturn(200);

        boolean result = service.deleteById(1L);

        Assertions.assertTrue(result);
        verify(mockConnection).disconnect();
    }

    @Test
    void testDeleteByIdFailure() throws Exception {
        when(mockConnection.getResponseCode()).thenReturn(400);

        boolean result = service.deleteById(1L);

        Assertions.assertFalse(result);
    }

    @Test
    void testUpdateStepByIdSuccess() throws Exception {
        // Preparazione dati di input
        Duration duration = Duration.ofMinutes(30);
        String semifinishedName = "semifinished";
        String machineryName = "machinery1";
        Machinery machinery = new Machinery();
        machinery.setId(Math.toIntExact(1L));
        when(mockMachineryService.getMachineryByName(machineryName)).thenReturn(machinery);

        byte[] json = mapper.writeValueAsBytes(new ModelWithStepsDTO.ProcessStepDTO());
        ByteArrayInputStream in = new ByteArrayInputStream(json);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(mockConnection.getOutputStream()).thenReturn(out);
        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getInputStream()).thenReturn(in);

        boolean result = service.updateStepById(1L, duration, semifinishedName, machineryName);

        Assertions.assertTrue(result);
        verify(mockConnection).getResponseCode();
    }



    @Test
    void testAddNewStepToModelSuccess() throws Exception {
        // Prepazione dati di input
        String modelName = "model1";
        Duration duration = Duration.ofMinutes(30);
        String semifinishedName = "semifinished";
        String machineryName = "machinery1";
        int insertAfterOrder = 1;

        byte[] json = mapper.writeValueAsBytes(new ModelWithStepsDTO.ProcessStepDTO());
        ByteArrayInputStream in = new ByteArrayInputStream(json);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(mockConnection.getOutputStream()).thenReturn(out);
        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getInputStream()).thenReturn(in);

        ProcessStep result = service.addNewStepToModel(modelName, duration, semifinishedName, machineryName, insertAfterOrder);

        Assertions.assertNotNull(result);
        verify(mockConnection).getResponseCode();
    }

    @Test
    void testAddNewStepToModelFailure() throws Exception {
        when(mockConnection.getResponseCode()).thenReturn(500);

        Assertions.assertThrows(IOException.class,
                () -> service.addNewStepToModel("model1", Duration.ofMinutes(30), "semifinished", "machinery1", 1));
    }
}
