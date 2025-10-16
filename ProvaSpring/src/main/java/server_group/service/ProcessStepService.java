package server_group.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server_group.model.Model;
import server_group.model.ProcessStep;
import server_group.model.Machinery;
import server_group.repository.MachineryRepository;
import server_group.repository.ModelRepository;
import server_group.repository.ProcessStepRepository;

import java.time.Duration;
import java.util.List;
import java.util.Comparator;
import java.util.Optional;

@Service
public class ProcessStepService {
    private final ProcessStepRepository processStepRepository;
    private final ModelRepository modelRepository;
    private final MachineryRepository machineryRepository;

    public ProcessStepService(ProcessStepRepository processStepRepository, ModelRepository modelRepository, MachineryRepository machineryRepository) {
        this.processStepRepository = processStepRepository;
        this.modelRepository = modelRepository;
        this.machineryRepository = machineryRepository;
    }

    public List<ProcessStep> findAll() { return processStepRepository.findAll(); }

    public List<ProcessStep> findByModelNameOrderByStepOrder(String modelName) {
        return processStepRepository.findByModelNameOrderByStepOrder(modelName);
    }

    public void deleteStep(Long id) {
        // recupera lo step da eliminare
        Optional<ProcessStep> optionalStep = processStepRepository.findById(id);

        if (optionalStep.isPresent()) {
            ProcessStep stepToDelete = optionalStep.get();
            String modelName = stepToDelete.getModel().getName();
            int stepOrder = stepToDelete.getStepOrder();

            // elimina lo step
            processStepRepository.deleteById(id);

            // recupera gli altri step di quel modello con stepOrder maggiore di quello eliminato
            List<ProcessStep> steps = processStepRepository.findByModelName(modelName);
            steps.stream()
                    .filter(s -> s.getStepOrder() > stepOrder)
                    .sorted(Comparator.comparingInt(ProcessStep::getStepOrder))
                    .forEach(s -> {
                        s.setStepOrder(s.getStepOrder() - 1);
                        processStepRepository.save(s);
                    });

        } else {
            throw new EntityNotFoundException("Step non trovato con id: " + id);
        }
    }

    public void updateStep(Long id, ProcessStep updatedStep) throws Exception {
        ProcessStep step = processStepRepository.findById(id)
                .orElseThrow(() -> new Exception("Step not found with id: " + id));

        step.setDuration(updatedStep.getDuration());
        step.setSemifinishedName(updatedStep.getSemifinishedName());
        step.setMachinery(updatedStep.getMachinery());

        processStepRepository.save(step);
    }

    public ProcessStep addStepToModel(String modelName, Duration duration, String semifinishedName, String machineryName, int insertAfterOrder) {
        Model model = modelRepository.findByName(modelName)
                .orElseThrow(() -> new IllegalArgumentException("Model not found with name: " + modelName));

        Machinery machinery = machineryRepository.findByName(machineryName)
                .orElseThrow(() -> new IllegalArgumentException("Machinery not found: " + machineryName));

        // sposta in avanti tutti gli step successivi
        List<ProcessStep> stepsToShift = processStepRepository.findByModelAndStepOrderGreaterThanEqualOrderByStepOrderDesc(model, insertAfterOrder + 1);
        for (ProcessStep step : stepsToShift) {
            step.setStepOrder(step.getStepOrder() + 1);
        }
        processStepRepository.saveAll(stepsToShift);

        // crea il nuovo step
        ProcessStep newStep = new ProcessStep();
        newStep.setModel(model);
        newStep.setStepOrder(insertAfterOrder + 1);
        newStep.setDuration(duration);
        newStep.setSemifinishedName(semifinishedName);
        newStep.setMachinery(machinery);

        return processStepRepository.save(newStep);

    }
}
