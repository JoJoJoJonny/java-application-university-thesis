package client_group.service;

import client_group.dto.NoticeDTO;
import org.junit.jupiter.api.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import static org.mockito.Mockito.*;

class NoticeServiceTest {

    private HttpURLConnection mockConnection;
    private Supplier<HttpURLConnection> connectionSupplier;
    private NoticeService service;

    @BeforeEach
    void setUp() {
        mockConnection = mock(HttpURLConnection.class);
        connectionSupplier = () -> mockConnection;
        service = new NoticeService(connectionSupplier);
    }

    @Test
    void testSaveNoticeSuccess() throws Exception {
        NoticeDTO notice = new NoticeDTO("creator1@example.com", "Notice 1", "This is a notice.");
        notice.setId(1L);
        notice.setCreatorFullName("John Doe");
        notice.setCategory("MyNotice");

        String fakeJson = "{\"id\":1,\"creatorEmail\":\"creator1@example.com\",\"creatorFullName\":\"John Doe\",\"subject\":\"Notice 1\",\"description\":\"This is a notice.\",\"category\":\"MyNotice\"}";

        when(mockConnection.getResponseCode()).thenReturn(201);
        when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream(fakeJson.getBytes()));
        OutputStream mockOutputStream = mock(OutputStream.class);
        when(mockConnection.getOutputStream()).thenReturn(mockOutputStream);

        Optional<NoticeDTO> result = service.saveNotice(notice);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("Notice 1", result.get().getSubject());
        Assertions.assertEquals("MyNotice", result.get().getCategory());
        Assertions.assertEquals("creator1@example.com", result.get().getCreatorEmail());
        verify(mockConnection).getOutputStream();
    }

    @Test
    void testSaveNoticeFailureNon2xxResponse() throws Exception {
        NoticeDTO notice = new NoticeDTO("creator1@example.com", "Notice 2", "This is another notice.");
        notice.setId(2L);
        notice.setCreatorFullName("Jane Doe");
        notice.setCategory("DelayNotice");

        when(mockConnection.getResponseCode()).thenReturn(500);
        OutputStream mockOutputStream = mock(OutputStream.class);
        when(mockConnection.getOutputStream()).thenReturn(mockOutputStream);

        Optional<NoticeDTO> result = service.saveNotice(notice);

        // Verifica che il risultato sia vuoto
        Assertions.assertTrue(result.isEmpty());
        verify(mockConnection).getOutputStream();
    }

    @Test
    void testSaveNoticeException() throws Exception {
        NoticeDTO notice = new NoticeDTO("creator1@example.com", "Notice 3", "This is an error case.");
        notice.setId(3L);
        notice.setCreatorFullName("Alice");
        notice.setCategory("EmployeesNotice");

        when(mockConnection.getOutputStream()).thenThrow(new IOException("Network error"));

        Optional<NoticeDTO> result = service.saveNotice(notice);

        // Verifica che il risultato sia vuoto a causa dell'eccezione
        Assertions.assertTrue(result.isEmpty());
        verify(mockConnection).getOutputStream();
    }

    @Test
    void testFetchNoticesException() throws Exception {
        String userEmail = "user@example.com";

        when(mockConnection.getResponseCode()).thenThrow(new IOException("Network error"));

        List<NoticeDTO> result = service.fetchNotices(userEmail);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void testDefaultConstructor() {
        NoticeService serviceDefault = new NoticeService();
        Assertions.assertNotNull(serviceDefault);
    }

    @Test
    void testFetchNoticesCallsDisconnect() throws Exception {
        String userEmail = "user@example.com";
        String fakeJson = "[{\"id\":1,\"creatorEmail\":\"creator1@example.com\",\"creatorFullName\":\"John Doe\",\"subject\":\"Notice 1\",\"description\":\"This is a notice.\",\"category\":\"MyNotice\"}]";

        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream(fakeJson.getBytes()));

        List<NoticeDTO> result = service.fetchNotices(userEmail);

        Assertions.assertFalse(result.isEmpty());
        verify(mockConnection).disconnect();
    }




    @Test
    void testFetchNoticesSuccess() throws Exception {
        String userEmail = "user@example.com";
        String fakeJson = "[{\"id\":1,\"creatorEmail\":\"creator1@example.com\",\"creatorFullName\":\"John Doe\",\"subject\":\"Notice 1\",\"description\":\"This is a notice.\",\"category\":\"MyNotice\"}]";

        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream(fakeJson.getBytes()));

        List<NoticeDTO> result = service.fetchNotices(userEmail);

        // Verifica che l'elenco degli avvisi sia correttamente restituito
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Notice 1", result.get(0).getSubject());
        verify(mockConnection).getInputStream();
    }

    @Test
    void testFetchNoticesFailure() throws Exception {
        String userEmail = "user@example.com";

        when(mockConnection.getResponseCode()).thenReturn(500);
        List<NoticeDTO> result = service.fetchNotices(userEmail);

        // Verifica che venga restituito un elenco vuoto in caso di errore
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void testDeleteNoticeSuccess() throws Exception {
        long noticeId = 1L;

        when(mockConnection.getResponseCode()).thenReturn(200);

        boolean result = service.deleteNotice(noticeId);

        // Verifica che il metodo ritorni true per successo
        Assertions.assertTrue(result);
        verify(mockConnection).getResponseCode();
    }

    @Test
    void testDeleteNoticeFailure() throws Exception {
        long noticeId = 1L;

        when(mockConnection.getResponseCode()).thenReturn(404);
        boolean result = service.deleteNotice(noticeId);

        // Verifica che il metodo ritorni false per errore
        Assertions.assertFalse(result);
        verify(mockConnection).getResponseCode();
    }

    @Test
    void testDeleteNoticeException() throws Exception {
        long noticeId = 1L;

        when(mockConnection.getResponseCode()).thenThrow(new IOException("Network error"));

        boolean result = service.deleteNotice(noticeId);

        // Verifica che il metodo ritorni false in caso di eccezione
        Assertions.assertFalse(result);
    }

}
