package server_group.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server_group.dto.AssignedTaskDTO;
import server_group.model.ProcessStepExecution;
import server_group.repository.ProcessStepExecutionRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProcessStepExecutionService {
    private final ProcessStepExecutionRepository processStepExecutionRepository;

    public ProcessStepExecutionService(ProcessStepExecutionRepository processStepExecutionRepository) {
        this.processStepExecutionRepository = processStepExecutionRepository;
    }

    public List<AssignedTaskDTO> getAssignedTasksToday(String email) {
        LocalDate today = LocalDate.now();
        List<ProcessStepExecution> tasks = processStepExecutionRepository.findAssignedToday(email, today);

        return tasks.stream()
                .map(AssignedTaskDTO::new)
                .toList();
    }

    public List<AssignedTaskDTO> getAssignedTasks(String email) {
        List<ProcessStepExecution> tasks = processStepExecutionRepository.findAssigned(email);

        return tasks.stream()
                .map(AssignedTaskDTO::new)
                .toList();
    }
}
