package server_group.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import server_group.model.Raw;
import server_group.service.RawService;

import java.util.List;
import java.util.Optional;

@RestController
@PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
@RequestMapping("/api/raw")
@CrossOrigin(origins = "*")
public class RawController {

    private final RawService rawService;

    public RawController(RawService rawService) {
        this.rawService = rawService;
    }

    @GetMapping
    public ResponseEntity<?> getAllRaw() {
        List<Raw> rawList = rawService.findAll();
        return ResponseEntity.ok(rawList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRawById(@PathVariable Long id) {
        Optional<Raw> rawOpt = rawService.findById(id);
        if (rawOpt.isPresent()) {
            return ResponseEntity.ok(rawOpt.get());
        } else {
            return ResponseEntity.status(404).body("Raw not found");
        }
    }


    @PostMapping
    public ResponseEntity<?> createRaw(@RequestBody Raw raw) {
        try {
            Raw created = rawService.save(raw);
            return ResponseEntity.status(201).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create raw: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRaw(@PathVariable Long id, @RequestBody Raw updatedRaw) {
        Optional<Raw> existing = rawService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(404).body("Raw not found");
        }

        updatedRaw.setId(id);
        Raw saved = rawService.save(updatedRaw);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRaw(@PathVariable Long id) {
        Optional<Raw> existing = rawService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(404).body("Raw not found");
        }

        rawService.delete(id);
        return ResponseEntity.ok("Raw deleted successfully");
    }
}
