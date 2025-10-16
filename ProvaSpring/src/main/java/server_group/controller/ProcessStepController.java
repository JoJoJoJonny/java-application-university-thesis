package server_group.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import server_group.model.ProcessStep;
import server_group.service.ProcessStepService;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/process")
public class ProcessStepController {

    private final ProcessStepService processStepService;

    public ProcessStepController(ProcessStepService processStepService) {
        this.processStepService = processStepService;
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE')")
    @GetMapping
    public List<ProcessStep> getAll() { return processStepService.findAll(); }

    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE')")
    @GetMapping("/{modelName}")
    public List<ProcessStep> getStepsByModel(@PathVariable String modelName) {
        return processStepService.findByModelNameOrderByStepOrder(modelName);
    }

    @PreAuthorize("hasAnyRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStep(@PathVariable Long id) {
        processStepService.deleteStep(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('MANAGER')")
    @PutMapping("/{id}/updateStep")
    public ResponseEntity<Void> updateStep(@PathVariable Long id, @RequestBody ProcessStep updatedStep) {
        try {
            processStepService.updateStep(id, updatedStep);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.ok().build();
        }
    }

    @PreAuthorize("hasAnyRole('MANAGER')")
    @PostMapping("/add")
    public ResponseEntity<ProcessStep> addStep(
            @RequestParam String modelName,
            @RequestParam int afterOrder,
            @RequestParam String duration, // ISO string
            @RequestParam String semifinishedName,
            @RequestParam String machineryName) {

        Duration dur = Duration.parse(duration);

        ProcessStep newStep = processStepService.addStepToModel(modelName, dur, semifinishedName, machineryName, afterOrder);

        return ResponseEntity.ok(newStep);
    }



}

