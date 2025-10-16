package server_group.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import server_group.model.Client;
import server_group.service.ClientService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "*")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
    @GetMapping
    public ResponseEntity<?> getAllClients() {
        List<Client> clientList = clientService.findAll();
        return ResponseEntity.ok(clientList);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
    @GetMapping("/{piva}")
    public ResponseEntity<?> getClientByPiva(@PathVariable String piva) {
        Optional<Client> clientOpt = clientService.findByPiva(piva);
        if (clientOpt.isPresent()) {
            return ResponseEntity.ok(clientOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Client not found!");
        }
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
    @PostMapping("/create_client")
    public ResponseEntity<?> createClient(@RequestBody Client client) {
        try {
            Client created = clientService.save(client);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create client: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
    @PutMapping("/{piva}")
    public ResponseEntity<?> updateClient(@PathVariable String piva, @RequestBody Client updatedClient) {
        Optional<Client> existing = clientService.findByPiva(piva);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Client not found!");
        }

        updatedClient.setPiva(piva);
        Client saved = clientService.save(updatedClient);
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
    @DeleteMapping("/{piva}")
    public ResponseEntity<?> deleteClient(@PathVariable String piva) {
        Optional<Client> existing = clientService.findByPiva(piva);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Client not found!");
        }

        clientService.deleteByPiva(piva);
        return ResponseEntity.ok("Client deleted successfully");
    }

}
