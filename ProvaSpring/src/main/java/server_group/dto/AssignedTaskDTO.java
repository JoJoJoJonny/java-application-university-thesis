package server_group.dto;

import server_group.model.ProcessStepExecution;

import java.time.LocalDate;

public class AssignedTaskDTO {
    private Long orderId;
    private String machineryName;
    private int stepIndex;
    private LocalDate scheduledStart;
    private LocalDate scheduledEnd;

    public AssignedTaskDTO(ProcessStepExecution execution) {
        this.orderId = execution.getOrder().getId();
        this.machineryName = execution.getMachineryName();
        this.stepIndex = execution.getStepIndex();
        this.scheduledStart = execution.getScheduledStart();
        this.scheduledEnd = execution.getScheduledEnd();
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getMachineryName() {
        return machineryName;
    }

    public void setMachineryName(String machineryName) {
        this.machineryName = machineryName;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(int stepIndex) {
        this.stepIndex = stepIndex;
    }

    public LocalDate getScheduledStart() {
        return scheduledStart;
    }

    public void setScheduledStart(LocalDate scheduledStart) {
        this.scheduledStart = scheduledStart;
    }

    public LocalDate getScheduledEnd() {
        return scheduledEnd;
    }

    public void setScheduledEnd(LocalDate scheduledEnd) {
        this.scheduledEnd = scheduledEnd;
    }
}
