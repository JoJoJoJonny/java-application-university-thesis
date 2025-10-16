package server_group.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server_group.dto.GanttBlockDTO;
import server_group.dto.ModelWithStepsDTO;
import server_group.model.Order;
import server_group.model.OrderStatus;
import server_group.model.ProcessStep;
import server_group.model.ProcessStepExecution;
import server_group.model.CustomUser;
import server_group.repository.CustomUserRepository;
import server_group.repository.OrderRepository;
import server_group.repository.ProcessStepExecutionRepository;
import server_group.repository.ProcessStepRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GanttService {

    private final ProcessStepExecutionRepository executionRepository;
    private final CustomUserRepository customUserRepository;

    public GanttService(ProcessStepExecutionRepository executionRepository, CustomUserRepository customUserRepository) {
        this.executionRepository = executionRepository;
        this.customUserRepository = customUserRepository;
    }

    /*
    private List<GanttBlockDTO> calculateGanttFromOrder(Order order, ModelWithStepsDTO modelDto) {
        List<GanttBlockDTO> blocks = new ArrayList<>();

        if (order.getStartDate() == null) {
            throw new IllegalStateException("startDate missing for and order IN_PRODUCTION");
        }

        LocalDate currentDate = order.getStartDate();
        int quantity = order.getQuantity();

        for (ModelWithStepsDTO.ProcessStepDTO step : modelDto.getProcessSteps()) {
            // ottieni la durata per pezzo in secondi
            long durationPerPieceSeconds= step.getDuration().toSeconds();
            // calcola la durata totale in secondi
            long totalSeconds = durationPerPieceSeconds * quantity;
            // converti in giorni (86400 secondi in un giorno, ma si lavora solo 8 ore quindi 28800)
            long durationDays = (long) Math.ceil(totalSeconds / 28800.0);

            if (durationDays < 1) {
                durationDays = 1;
            }


            LocalDate start = currentDate;
            LocalDate end = currentDate.plusDays(durationDays);

            GanttBlockDTO block = new GanttBlockDTO();
            block.setMachineryName(step.getMachinery().getName());
            block.setStepName(step.getSemifinishedName());
            block.setStart(start);
            block.setEnd(end);
            block.setStepOrder(step.getStepOrder());

            blocks.add(block);
            currentDate = end; // step sequenziali
        }

        return blocks;
    }*/

    /* //era solo una prova, per un unico ordine, non dovrebbe servirmi più
    public List<GanttBlockDTO> getGanttForOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        if (order.getStatus() != OrderStatus.IN_PRODUCTION) {
            throw new IllegalStateException("Order not in production!");
        }

        ModelWithStepsDTO modelDto = modelService.getAllModelsWithProcess().stream()
                .filter(m -> m.getName().equals(order.getModel().getName()))
                .findFirst()
                .orElseThrow();

        return calculateGanttFromOrder(order, modelDto);
    }*/

    public List<GanttBlockDTO> getAllScheduledGanttBlocks() {
        List<ProcessStepExecution> executions = executionRepository.findByOrderStatus(OrderStatus.IN_PRODUCTION);
        List<GanttBlockDTO> blocks = new ArrayList<>();

        // ordina per data di inizio
        executions.sort(Comparator.comparing(ProcessStepExecution::getScheduledStart));

        for (ProcessStepExecution exec : executions) {
            GanttBlockDTO block = new GanttBlockDTO();
            block.setExecutionId(exec.getId());
            block.setOrderId(exec.getOrder().getId());
            block.setMachineryName(exec.getMachineryName());
            block.setStepName("Step " + exec.getStepIndex());
            block.setScheduledStart(exec.getScheduledStart());
            block.setScheduledEnd(exec.getScheduledEnd());
            // inizialmente actual = scheduled
            block.setActualStart(exec.getActualStart());
            block.setActualEnd(exec.getActualEnd());
            block.setStepOrder(exec.getStepIndex());

            CustomUser assigned = exec.getAssignedEmployee();
            if (assigned != null) {
                block.setAssignedEmployeeEmail(assigned.getEmail());
                block.setAssignedEmployeeFullName(assigned.getName() + " " + assigned.getSurname());
            }

            blocks.add(block);
        }

        return blocks;
    }

    /* //non dovrebbe più servire in quanto è spostato in OrderProductionService
    private List<GanttBlockDTO> scheduleOrders(List<Order> orders, Map<String, ModelWithStepsDTO> modelMap) {
        List<GanttBlockDTO> allBlocks = new ArrayList<>();
        Map<String, LocalDate> machineAvailability = new HashMap<>();

        // Ordina per startDate crescente, poi deadline
        orders.sort(Comparator.comparing(Order::getStartDate).thenComparing(Order::getDeadline));

        for (Order order : orders) {
            ModelWithStepsDTO model = modelMap.get(order.getModel().getName());
            if (model == null) continue;

            LocalDate stepStartDate = order.getStartDate();

            for (ModelWithStepsDTO.ProcessStepDTO step : model.getProcessSteps()) {
                long totalSeconds = step.getDuration().toSeconds() * order.getQuantity();
                long daysNeeded = (long) Math.ceil(totalSeconds / 28800.0);

                String machineryName = step.getMachinery().getName();

                LocalDate machineAvailableFrom = machineAvailability.getOrDefault(machineryName, LocalDate.MIN);

                LocalDate actualStart = machineAvailableFrom.isAfter(stepStartDate) ? machineAvailableFrom : stepStartDate;
                LocalDate actualEnd = actualStart.plusDays(daysNeeded);

                if (actualEnd.isAfter(order.getDeadline())) {
                    throw new RuntimeException("Impossible to schedule order " + order.getId() + " before deadline.");
                }

                GanttBlockDTO block = new GanttBlockDTO();
                block.setMachineryName(machineryName);
                block.setStepName(step.getSemifinishedName());
                block.setStart(actualStart);
                block.setEnd(actualEnd);
                block.setStepOrder(step.getStepOrder());
                block.setOrderId(order.getId());

                allBlocks.add(block);

                machineAvailability.put(machineryName, actualEnd);
                stepStartDate = actualEnd;
            }
        }
        return allBlocks;
    }*/

    public void updateBlocks(List<GanttBlockDTO> modifiedBlocks) {
        for (GanttBlockDTO dto : modifiedBlocks) {
            Optional<ProcessStepExecution> optExecution = executionRepository.findById(dto.getExecutionId());
            if (optExecution.isPresent()) {
                ProcessStepExecution execution = optExecution.get();
                execution.setActualStart(dto.getActualStart());
                execution.setActualEnd(dto.getActualEnd());

                // assegnazione dell'employee se presente
                if (dto.getAssignedEmployeeEmail() != null && !dto.getAssignedEmployeeEmail().isBlank()) {
                    Optional<CustomUser> userOpt = customUserRepository.findByEmail(dto.getAssignedEmployeeEmail());
                    if (userOpt.isPresent()) {
                        CustomUser user = userOpt.get();
                        if ("Employee".equalsIgnoreCase(user.getRole().name())) {
                            execution.setAssignedEmployee(user);
                        } else {
                            throw new RuntimeException("L'utente " + user.getEmail() + " non è un Employee.");
                        }
                    } else {
                        throw new RuntimeException("Utente non trovato con email: " + dto.getAssignedEmployeeEmail());
                    }
                } else {
                    execution.setAssignedEmployee(null); // eventualmente rimuove l’assegnazione
                }
                executionRepository.save(execution);
            } else {
                throw new RuntimeException("Step non trovato per ID: " + dto.getExecutionId());
            }
        }
    }
}
