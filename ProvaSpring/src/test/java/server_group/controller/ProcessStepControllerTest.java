package server_group.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import server_group.model.ProcessStep;
import server_group.service.ProcessStepService;

import java.time.Duration;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProcessStepControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProcessStepService processStepService;

    @InjectMocks
    private ProcessStepController processStepController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(processStepController).build();
    }

    @Test
    void testGetAll() throws Exception {
        ProcessStep step1 = new ProcessStep();
        step1.setId(1L);
        ProcessStep step2 = new ProcessStep();
        step2.setId(2L);

        when(processStepService.findAll()).thenReturn(Arrays.asList(step1, step2));

        mockMvc.perform(get("/api/process"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(processStepService, times(1)).findAll();
    }

    @Test
    void testGetStepsByModel() throws Exception {
        ProcessStep step1 = new ProcessStep();
        step1.setId(1L);

        when(processStepService.findByModelNameOrderByStepOrder("model1"))
                .thenReturn(Arrays.asList(step1));

        mockMvc.perform(get("/api/process/model1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(processStepService, times(1))
                .findByModelNameOrderByStepOrder("model1");
    }

    @Test
    void testDeleteStep() throws Exception {
        mockMvc.perform(delete("/api/process/1"))
                .andExpect(status().isOk());

        verify(processStepService, times(1)).deleteStep(1L);
    }

    @Test
    void testUpdateStep_Success() throws Exception {
        ProcessStep updated = new ProcessStep();
        updated.setId(1L);

        doNothing().when(processStepService).updateStep(eq(1L), any(ProcessStep.class));

        mockMvc.perform(put("/api/process/1/updateStep")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk());

        verify(processStepService, times(1)).updateStep(eq(1L), any(ProcessStep.class));
    }

    @Test
    void testUpdateStep_Exception() throws Exception {
        ProcessStep updated = new ProcessStep();
        updated.setId(1L);

        doThrow(new RuntimeException("error"))
                .when(processStepService).updateStep(eq(1L), any(ProcessStep.class));

        mockMvc.perform(put("/api/process/1/updateStep")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk()); // ritorna comunque 200

        verify(processStepService, times(1)).updateStep(eq(1L), any(ProcessStep.class));
    }

    @Test
    void testAddStep() throws Exception {
        ProcessStep newStep = new ProcessStep();
        newStep.setId(1L);

        when(processStepService.addStepToModel(
                eq("model1"),
                eq(Duration.parse("PT1H")),
                eq("semi"),
                eq("machine"),
                eq(2)))
                .thenReturn(newStep);

        mockMvc.perform(post("/api/process/add")
                        .param("modelName", "model1")
                        .param("afterOrder", "2")
                        .param("duration", "PT1H")
                        .param("semifinishedName", "semi")
                        .param("machineryName", "machine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(processStepService, times(1)).addStepToModel(
                "model1", Duration.parse("PT1H"), "semi", "machine", 2);
    }
}
