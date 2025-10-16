package server_group.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server_group.dto.ModelWithStepsDTO;
import server_group.model.Order;
import server_group.model.OrderStatus;
import server_group.model.ProcessStepExecution;
import server_group.repository.OrderRepository;
import server_group.repository.ProcessStepExecutionRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderProductionService {

    private final OrderRepository orderRepository;
    private final ModelService modelService;
    private final ProcessStepExecutionRepository executionRepository;

    public OrderProductionService(OrderRepository orderRepository, ModelService modelService, ProcessStepExecutionRepository executionRepository) {
        this.orderRepository = orderRepository;
        this.modelService = modelService;
        this.executionRepository = executionRepository;
    }

    // questo è il punto in cui mando in produzione l'ordine
    public void startProduction(Order order) {
        if (order.getStatus() == OrderStatus.IN_PRODUCTION) {
            throw new IllegalStateException("Order already in production");
        }else if(order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Order already completed!");
        }else if(order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order already cancelled!");
        }
        order.setStatus(OrderStatus.IN_PRODUCTION);
        order.setStartDate(LocalDate.now());
        orderRepository.save(order);

        // genero gli execution schedulati
        createScheduledExecutions(order);
    }

    private void createScheduledExecutions(Order order) {
        // recupera modello e steps
        ModelWithStepsDTO modelDto = modelService.getAllModelsWithProcess().stream()
                .filter(m -> m.getName().equals(order.getModel().getName()))
                .findFirst()
                .orElseThrow();

        LocalDate stepStartDate = order.getStartDate();
        int quantity = order.getQuantity();

        // tiene traccia disponibilità macchine globalmente per evitare conflitti tra più ordini già in produzione
        Map<String, LocalDate> machineAvailability = new HashMap<>();
        // carica disponibilità attuale da esecuzioni esistenti di altri ordini IN_PRODUCTION
        List<ProcessStepExecution> existing = executionRepository.findAll();
        for (ProcessStepExecution e : existing) {
            LocalDate end = e.getScheduledEnd();
            machineAvailability.merge(e.getMachineryName(), end, (old, neu) -> old.isAfter(neu) ? old : neu);
        }

        for (ModelWithStepsDTO.ProcessStepDTO step : modelDto.getProcessSteps()) {
            long totalSeconds = step.getDuration().toSeconds() * quantity;
            long daysNeeded = (long) Math.ceil(totalSeconds / 28800.0);
            if (daysNeeded < 1) daysNeeded = 1;

            String machineryName = step.getMachinery().getName();

            LocalDate machineAvailableFrom = machineAvailability.getOrDefault(machineryName, LocalDate.MIN);
            LocalDate actualStart = machineAvailableFrom.isAfter(stepStartDate) ? machineAvailableFrom : stepStartDate;
            LocalDate actualEnd = actualStart.plusDays(daysNeeded);

            if (actualEnd.isAfter(order.getDeadline())) {
                throw new RuntimeException("Cannot schedule order " + order.getId() + " before deadline.");
            }

            ProcessStepExecution exec = new ProcessStepExecution();
            exec.setOrder(order);
            exec.setMachineryName(machineryName);
            exec.setStepIndex(step.getStepOrder());
            exec.setScheduledStart(actualStart);
            exec.setScheduledEnd(actualEnd);
            // actualStart/End vengono impostati uguali agli scheduled e modificati dall'utente
            exec.setActualStart(actualStart);
            exec.setActualEnd(actualEnd);
            executionRepository.save(exec);

            // aggiorna stato
            machineAvailability.put(machineryName, actualEnd);
            stepStartDate = actualEnd;
        }
    }

    public void updateExecutionDates(Long executionId, LocalDate actualStart, LocalDate actualEnd) {
        ProcessStepExecution exec = executionRepository.findById(executionId).orElseThrow();
        exec.setActualStart(actualStart);
        exec.setActualEnd(actualEnd);
        executionRepository.save(exec);
    }

    public void deleteAllExecutions(Order order) {
        executionRepository.deleteByOrderId(order.getId());
        order.setEndDate(LocalDate.now());
        orderRepository.save(order);
    }

}

