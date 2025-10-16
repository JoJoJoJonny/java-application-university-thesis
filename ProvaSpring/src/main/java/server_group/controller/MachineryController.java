package server_group.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import server_group.model.Machinery;
import server_group.service.MachineryService;

import java.util.List;
import java.util.Optional;

@RestController
@PreAuthorize("hasAnyRole('MANAGER')")
@RequestMapping("/api/machinery")
@CrossOrigin(origins = "*")
public class MachineryController {

    private final MachineryService machineryService;

    public MachineryController(MachineryService machineryService) {
        this.machineryService = machineryService;
    }

    @GetMapping
    public ResponseEntity<?> getAllMachinery() {
        List<Machinery> machineryList = machineryService.findAll();
        return ResponseEntity.ok(machineryList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMachineryById(@PathVariable Long id) {
        Optional<Machinery> machineryOpt = machineryService.findById(id);
        return machineryOpt
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("Machinery not found"));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<?> getMachineryByName(@PathVariable String name) {
        Optional<Machinery> machineryOpt = machineryService.findByName(name);
        return machineryOpt
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("Machinery with name '" + name + "' not found"));
    }

    @PostMapping
    public ResponseEntity<?> createMachinery(@RequestBody Machinery machinery) {
        try {
            Machinery created = machineryService.save(machinery);
            return ResponseEntity.status(201).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create machinery: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMachinery(@PathVariable Long id, @RequestBody Machinery updatedMachinery) {
        Optional<Machinery> existing = machineryService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(404).body("Machinery not found");
        }

        updatedMachinery.setId(id);
        Machinery saved = machineryService.save(updatedMachinery);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMachinery(@PathVariable Long id) {
        Optional<Machinery> existing = machineryService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(404).body("Machinery not found");
        }

        machineryService.delete(id);
        return ResponseEntity.ok("Machinery deleted successfully");
    }
}
