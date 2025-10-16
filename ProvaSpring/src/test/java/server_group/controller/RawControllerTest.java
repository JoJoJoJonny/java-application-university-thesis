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
import server_group.model.Raw;
import server_group.service.RawService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RawControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RawService rawService;

    @InjectMocks
    private RawController rawController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(rawController).build();
    }

    @Test
    void testGetAllRaw() throws Exception {
        Raw raw1 = new Raw();
        raw1.setId(1L);
        Raw raw2 = new Raw();
        raw2.setId(2L);
        List<Raw> raws = Arrays.asList(raw1, raw2);

        when(rawService.findAll()).thenReturn(raws);

        mockMvc.perform(get("/api/raw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(rawService, times(1)).findAll();
    }

    @Test
    void testGetRawById_Found() throws Exception {
        Raw raw = new Raw();
        raw.setId(1L);

        when(rawService.findById(1L)).thenReturn(Optional.of(raw));

        mockMvc.perform(get("/api/raw/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(rawService, times(1)).findById(1L);
    }

    @Test
    void testGetRawById_NotFound() throws Exception {
        when(rawService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/raw/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Raw not found"));

        verify(rawService, times(1)).findById(1L);
    }

    @Test
    void testCreateRaw_Success() throws Exception {
        Raw raw = new Raw();
        raw.setId(1L);

        when(rawService.save(any(Raw.class))).thenReturn(raw);

        mockMvc.perform(post("/api/raw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(raw)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));

        verify(rawService, times(1)).save(any(Raw.class));
    }

    @Test
    void testCreateRaw_Failure() throws Exception {
        Raw raw = new Raw();

        when(rawService.save(any(Raw.class))).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/api/raw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(raw)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Failed to create raw")));

        verify(rawService, times(1)).save(any(Raw.class));
    }

    @Test
    void testUpdateRaw_Found() throws Exception {
        Raw existing = new Raw();
        existing.setId(1L);
        Raw updated = new Raw();
        updated.setId(1L);

        when(rawService.findById(1L)).thenReturn(Optional.of(existing));
        when(rawService.save(any(Raw.class))).thenReturn(updated);

        mockMvc.perform(put("/api/raw/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(rawService, times(1)).findById(1L);
        verify(rawService, times(1)).save(any(Raw.class));
    }

    @Test
    void testUpdateRaw_NotFound() throws Exception {
        Raw updated = new Raw();

        when(rawService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/raw/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Raw not found"));

        verify(rawService, times(1)).findById(1L);
        verify(rawService, times(0)).save(any(Raw.class));
    }

    @Test
    void testDeleteRaw_Found() throws Exception {
        Raw existing = new Raw();
        existing.setId(1L);

        when(rawService.findById(1L)).thenReturn(Optional.of(existing));
        doNothing().when(rawService).delete(1L);

        mockMvc.perform(delete("/api/raw/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Raw deleted successfully"));

        verify(rawService, times(1)).findById(1L);
        verify(rawService, times(1)).delete(1L);
    }

    @Test
    void testDeleteRaw_NotFound() throws Exception {
        when(rawService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/raw/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Raw not found"));

        verify(rawService, times(1)).findById(1L);
        verify(rawService, times(0)).delete(anyLong());
    }
}
