package server_group.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server_group.model.Client;
import server_group.repository.ClientRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- findAll ---
    @Test
    void findAll_returnsList() {
        // Arrange
        Client client = new Client();
        client.setPiva("12345678901");
        when(clientRepository.findAll()).thenReturn(List.of(client));

        // Act
        List<Client> result = clientService.findAll();

        // Assert
        assertEquals(1, result.size());
        assertEquals("12345678901", result.get(0).getPiva());
        verify(clientRepository, times(1)).findAll();
    }

    // --- findByPiva ---
    @Test
    void findByPiva_found() {
        // Arrange
        Client client = new Client();
        client.setPiva("IT123456");
        when(clientRepository.findById("IT123456")).thenReturn(Optional.of(client));

        // Act
        Optional<Client> result = clientService.findByPiva("IT123456");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("IT123456", result.get().getPiva());
        verify(clientRepository, times(1)).findById("IT123456");
    }

    @Test
    void findByPiva_notFound() {
        // Arrange
        when(clientRepository.findById("MISSING")).thenReturn(Optional.empty());

        // Act
        Optional<Client> result = clientService.findByPiva("MISSING");

        // Assert
        assertFalse(result.isPresent());
        verify(clientRepository, times(1)).findById("MISSING");
    }

    // --- save ---
    @Test
    void save_client_success() {
        // Arrange
        Client client = new Client();
        client.setPiva("987654321");
        when(clientRepository.save(client)).thenReturn(client);

        // Act
        Client result = clientService.save(client);

        // Assert
        assertNotNull(result);
        assertEquals("987654321", result.getPiva());
        verify(clientRepository, times(1)).save(client);
    }

    // --- deleteByPiva ---
    @Test
    void deleteByPiva_callsRepository() {
        // Arrange
        String piva = "111222333";
        doNothing().when(clientRepository).deleteById(piva);

        // Act
        clientService.deleteByPiva(piva);

        // Assert
        verify(clientRepository, times(1)).deleteById(piva);
    }
}
