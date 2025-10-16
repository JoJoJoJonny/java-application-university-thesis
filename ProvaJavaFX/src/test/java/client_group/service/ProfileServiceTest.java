package client_group.service;

import client_group.dto.AssignedTaskDTO;
import client_group.dto.ProfileDTO;
import client_group.model.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileServiceTest {

    @Mock
    private HttpURLConnection mockConnection;

    private ProfileService service;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final String token = "mockToken";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Session.getInstance().setToken(token);

        // Factory injection
        service = new ProfileService(url -> mockConnection);
    }

    @Test
    void testGetProfileByEmail_success() throws Exception {
        ProfileDTO mockProfile = new ProfileDTO();
        mockProfile.setEmail("test@example.com");
        mockProfile.setName("Test");
        mockProfile.setSurname("User");
        mockProfile.setPhone("1234567890");
        mockProfile.setRole("Admin");

        String jsonResponse = new JSONObject()
                .put("email", mockProfile.getEmail())
                .put("name", mockProfile.getName())
                .put("surname", mockProfile.getSurname())
                .put("phone", mockProfile.getPhone())
                .put("role", mockProfile.getRole())
                .toString();

        InputStream inputStream = new ByteArrayInputStream(jsonResponse.getBytes(StandardCharsets.UTF_8));
        when(mockConnection.getInputStream()).thenReturn(inputStream);
        when(mockConnection.getResponseCode()).thenReturn(200);

        ProfileDTO result = service.getProfileByEmail("test@example.com");

        assertNotNull(result);
        assertEquals(mockProfile.getEmail(), result.getEmail());
        assertEquals(mockProfile.getName(), result.getName());
        assertEquals(mockProfile.getSurname(), result.getSurname());
        assertEquals(mockProfile.getPhone(), result.getPhone());
        assertEquals(mockProfile.getRole(), result.getRole());
        verify(mockConnection).setRequestProperty("Authorization", "Bearer " + token);
    }

    @Test
    void testUpdateProfile_success() throws Exception {
        ProfileDTO profile = new ProfileDTO();
        profile.setEmail("test@example.com");
        profile.setName("Updated Name");
        profile.setSurname("Updated Surname");
        profile.setPhone("0987654321");
        profile.setRole("User");

        // mock OutputStream
        OutputStream outputStream = mock(OutputStream.class);
        when(mockConnection.getOutputStream()).thenReturn(outputStream);

        // mock InputStream
        String jsonResponse = new JSONObject()
                .put("email", profile.getEmail())
                .put("name", profile.getName())
                .put("surname", profile.getSurname())
                .put("phone", profile.getPhone())
                .put("role", profile.getRole())
                .toString();

        InputStream inputStream = new ByteArrayInputStream(jsonResponse.getBytes(StandardCharsets.UTF_8));
        when(mockConnection.getInputStream()).thenReturn(inputStream);

        // response code mock
        when(mockConnection.getResponseCode()).thenReturn(200);

        ProfileDTO result = service.updateProfile(profile);

        assertNotNull(result);
        verify(mockConnection).setRequestMethod("PUT");
        verify(mockConnection).setDoOutput(true);
        verify(mockConnection).setRequestProperty("Content-Type", "application/json");
        verify(mockConnection).setRequestProperty("Authorization", "Bearer " + token);
        verify(mockConnection).getOutputStream();
        verify(outputStream).write(any(byte[].class));
        verify(outputStream).flush();
    }


    @Test
    void testDeleteProfile_success() throws Exception {
        String email = "test@example.com";
        when(mockConnection.getResponseCode()).thenReturn(200);

        service.deleteProfile(email);

        verify(mockConnection).setRequestMethod("DELETE");
        verify(mockConnection).setRequestProperty("Authorization", "Bearer " + token);
    }

    @Test
    void testGetAssignedTasksToday_success() throws Exception {
        AssignedTaskDTO task1 = new AssignedTaskDTO();
        task1.setOrderId(1L);
        task1.setMachineryName("Mach1");
        task1.setStepIndex(0);
        task1.setScheduledStart(LocalDate.of(2025, 9, 1));
        task1.setScheduledEnd(LocalDate.of(2025, 9, 2));

        AssignedTaskDTO task2 = new AssignedTaskDTO();
        task2.setOrderId(2L);
        task2.setMachineryName("Mach2");
        task2.setStepIndex(1);
        task2.setScheduledStart(LocalDate.of(2025, 9, 3));
        task2.setScheduledEnd(LocalDate.of(2025, 9, 4));

        AssignedTaskDTO[] tasksArray = {task1, task2};
        String jsonResponse = mapper.writeValueAsString(tasksArray);

        InputStream inputStream = new ByteArrayInputStream(jsonResponse.getBytes(StandardCharsets.UTF_8));
        when(mockConnection.getInputStream()).thenReturn(inputStream);
        when(mockConnection.getResponseCode()).thenReturn(200);

        List<AssignedTaskDTO> tasks = service.getAssignedTasksToday("test@example.com");

        assertNotNull(tasks);
        assertEquals(2, tasks.size());
        assertEquals(task1.getOrderId(), tasks.get(0).getOrderId());
        assertEquals(task2.getOrderId(), tasks.get(1).getOrderId());
        assertEquals(task1.getMachineryName(), tasks.get(0).getMachineryName());
        assertEquals(task2.getMachineryName(), tasks.get(1).getMachineryName());
    }

    @Test
    void testGetAssignedTasks_success() throws Exception {
        AssignedTaskDTO task1 = new AssignedTaskDTO();
        task1.setOrderId(1L);
        task1.setMachineryName("Mach1");
        task1.setStepIndex(0);
        task1.setScheduledStart(LocalDate.of(2025, 9, 1));
        task1.setScheduledEnd(LocalDate.of(2025, 9, 2));

        AssignedTaskDTO task2 = new AssignedTaskDTO();
        task2.setOrderId(2L);
        task2.setMachineryName("Mach2");
        task2.setStepIndex(1);
        task2.setScheduledStart(LocalDate.of(2025, 9, 3));
        task2.setScheduledEnd(LocalDate.of(2025, 9, 4));

        AssignedTaskDTO[] tasksArray = {task1, task2};
        String jsonResponse = mapper.writeValueAsString(tasksArray);

        InputStream inputStream = new ByteArrayInputStream(jsonResponse.getBytes(StandardCharsets.UTF_8));
        when(mockConnection.getInputStream()).thenReturn(inputStream);
        when(mockConnection.getResponseCode()).thenReturn(200);

        List<AssignedTaskDTO> tasks = service.getAssignedTasks("test@example.com");

        assertNotNull(tasks);
        assertEquals(2, tasks.size());
        assertEquals(task1.getOrderId(), tasks.get(0).getOrderId());
        assertEquals(task2.getOrderId(), tasks.get(1).getOrderId());
        assertEquals(task1.getMachineryName(), tasks.get(0).getMachineryName());
        assertEquals(task2.getMachineryName(), tasks.get(1).getMachineryName());
    }
}
