package client_group.service;

import client_group.dto.GanttBlockDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;

class GanttServiceTest {

    private HttpURLConnection mockConnection;
    private Supplier<HttpURLConnection> connectionSupplier;
    private GanttService service;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mockConnection = mock(HttpURLConnection.class);
        connectionSupplier = () -> mockConnection;
        service = new GanttService(connectionSupplier);

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testGetGanttForAllOrdersSuccess() throws Exception {
        List<GanttBlockDTO> expected = List.of(
                new GanttBlockDTO(1L, 100L, "Macchina1", "Taglio",
                        LocalDate.now(), LocalDate.now().plusDays(1), 1),
                new GanttBlockDTO(2L, 200L, "Macchina2", "Saldatura",
                        LocalDate.now(), LocalDate.now().plusDays(2), 2)
        );

        byte[] json = mapper.writeValueAsBytes(expected);
        ByteArrayInputStream in = new ByteArrayInputStream(json);

        when(mockConnection.getInputStream()).thenReturn(in);

        List<GanttBlockDTO> result = service.getGanttForAllOrders();

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("Taglio", result.get(0).getStepName());
        Assertions.assertEquals("Saldatura", result.get(1).getStepName());
    }

    @Test
    void testSaveModifiedBlocksSuccess() throws Exception {
        List<GanttBlockDTO> modified = List.of(
                new GanttBlockDTO(3L, 300L, "Macchina3", "Assemblaggio",
                        LocalDate.now(), LocalDate.now().plusDays(3), 3)
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(mockConnection.getOutputStream()).thenReturn(out);
        when(mockConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);

        Assertions.assertDoesNotThrow(() -> service.saveModifiedBlocks(modified));

        String writtenJson = out.toString();
        Assertions.assertTrue(writtenJson.contains("Assemblaggio"));
    }

    @Test
    void testSaveModifiedBlocksHttpError() throws Exception {
        List<GanttBlockDTO> modified = List.of(
                new GanttBlockDTO(4L, 400L, "Macchina4", "Collaudo",
                        LocalDate.now(), LocalDate.now().plusDays(4), 4)
        );

        when(mockConnection.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockConnection.getResponseCode()).thenReturn(500);

        Assertions.assertThrows(Exception.class, () -> service.saveModifiedBlocks(modified));
    }

    @Test
    void testSaveModifiedBlocksOtherException() throws Exception {
        List<GanttBlockDTO> modified = List.of(
                new GanttBlockDTO(5L, 500L, "Macchina5", "Imballaggio",
                        LocalDate.now(), LocalDate.now().plusDays(5), 5)
        );

        // Simula eccezione generica
        when(mockConnection.getOutputStream()).thenThrow(new RuntimeException("Boom"));

        Assertions.assertThrows(Exception.class, () -> service.saveModifiedBlocks(modified));
    }
}
