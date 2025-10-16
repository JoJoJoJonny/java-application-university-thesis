package server_group.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import server_group.model.Client;
import server_group.service.ClientService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ClientControllerTest {

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientController clientController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- TEST GET ALL CLIENTS ---
    @Test
    void getAllClients_successful() {
        // Arrange
        Client client1 = new Client();
        Client client2 = new Client();
        List<Client> clients = Arrays.asList(client1, client2);
        when(clientService.findAll()).thenReturn(clients);

        // Act
        ResponseEntity<?> result = clientController.getAllClients();

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(clients, result.getBody());
        verify(clientService, times(1)).findAll();
    }

    // --- TEST GET CLIENT BY PIVA ---
    @Test
    void getClientByPiva_found() {
        // Arrange
        Client client = new Client();
        client.setPiva("123");
        when(clientService.findByPiva("123")).thenReturn(Optional.of(client));

        // Act
        ResponseEntity<?> result = clientController.getClientByPiva("123");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(client, result.getBody());
        verify(clientService, times(1)).findByPiva("123");
    }

    @Test
    void getClientByPiva_notFound() {
        // Arrange
        when(clientService.findByPiva("123")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> result = clientController.getClientByPiva("123");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Client not found!", result.getBody());
    }

    // --- TEST CREATE CLIENT ---
    @Test
    void createClient_successful() {
        // Arrange
        Client client = new Client();
        when(clientService.save(client)).thenReturn(client);

        // Act
        ResponseEntity<?> result = clientController.createClient(client);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(client, result.getBody());
        verify(clientService, times(1)).save(client);
    }

    @Test
    void createClient_failure() {
        // Arrange
        Client client = new Client();
        when(clientService.save(client)).thenThrow(new RuntimeException("DB error"));

        // Act
        ResponseEntity<?> result = clientController.createClient(client);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Failed to create client: DB error", result.getBody());
    }

    // --- TEST UPDATE CLIENT ---
    @Test
    void updateClient_found() {
        // Arrange
        Client client = new Client();
        client.setPiva("123");
        when(clientService.findByPiva("123")).thenReturn(Optional.of(client));
        when(clientService.save(client)).thenReturn(client);

        // Act
        ResponseEntity<?> result = clientController.updateClient("123", client);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(client, result.getBody());
        verify(clientService, times(1)).findByPiva("123");
        verify(clientService, times(1)).save(client);
    }

    @Test
    void updateClient_notFound() {
        // Arrange
        Client client = new Client();
        when(clientService.findByPiva("123")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> result = clientController.updateClient("123", client);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Client not found!", result.getBody());
    }

    // --- TEST DELETE CLIENT ---
    @Test
    void deleteClient_found() {
        // Arrange
        Client client = new Client();
        client.setPiva("123");
        when(clientService.findByPiva("123")).thenReturn(Optional.of(client));
        doNothing().when(clientService).deleteByPiva("123");

        // Act
        ResponseEntity<?> result = clientController.deleteClient("123");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Client deleted successfully", result.getBody());
        verify(clientService, times(1)).deleteByPiva("123");
    }

    @Test
    void deleteClient_notFound() {
        // Arrange
        when(clientService.findByPiva("123")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> result = clientController.deleteClient("123");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Client not found!", result.getBody());
    }
}
