package server_group.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import server_group.dto.GanttBlockDTO;
import server_group.model.ProcessStepExecution;
import server_group.service.GanttService;
import server_group.service.OrderProductionService;

import java.util.List;

@RestController
@RequestMapping("/api/gantt")
public class GanttController {

    private final GanttService ganttService;

    public GanttController(GanttService ganttService) {
        this.ganttService = ganttService;
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE')")
    @GetMapping("/orders/all")
    public ResponseEntity<List<GanttBlockDTO>> getAllGantt() {
        List<GanttBlockDTO> scheduledGanttBlock = ganttService.getAllScheduledGanttBlocks();
        return ResponseEntity.ok(scheduledGanttBlock);
    }

    @PreAuthorize("hasAnyRole('MANAGER')")
    @PostMapping("/update")
    public ResponseEntity<?> updateBlocks(@RequestBody List<GanttBlockDTO> modifiedBlocks) {
        try {
            ganttService.updateBlocks(modifiedBlocks);
            return ResponseEntity.ok(modifiedBlocks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
