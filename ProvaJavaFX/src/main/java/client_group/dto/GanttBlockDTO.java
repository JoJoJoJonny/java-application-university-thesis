package client_group.dto;

import java.time.LocalDate;

public class GanttBlockDTO {
    private Long executionId;        // l'id di ProcessStepExecution
    private Long orderId;
    private String machineryName;
    private String stepName;
    private LocalDate scheduledStart; // start pianificato
    private LocalDate scheduledEnd;   // end pianificato
    private LocalDate actualStart;    // start effettivo (può essere null finché non viene modificato)
    private LocalDate actualEnd;      // end effettivo
    private int stepOrder;
    private String assignedEmployeeEmail;
    private String assignedEmployeeFullName;

    //setter e getter

    public GanttBlockDTO() {
    }

    public GanttBlockDTO(Long executionId, Long orderId, String machineryName, String stepName,
                         LocalDate scheduledStart, LocalDate scheduledEnd, int stepOrder) {
        this.executionId = executionId;
        this.orderId = orderId;
        this.machineryName = machineryName;
        this.stepName = stepName;
        this.scheduledStart = scheduledStart;
        this.scheduledEnd = scheduledEnd;
        this.stepOrder = stepOrder;
        this.actualStart = scheduledStart;
        this.actualEnd = scheduledEnd;
    }

    public Long getExecutionId() {
        return executionId;
    }

    public void setExecutionId(Long executionId) {
        this.executionId = executionId;
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

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
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

    public LocalDate getActualStart() {
        return actualStart;
    }

    public void setActualStart(LocalDate actualStart) {
        this.actualStart = actualStart;
    }

    public LocalDate getActualEnd() {
        return actualEnd;
    }

    public void setActualEnd(LocalDate actualEnd) {
        this.actualEnd = actualEnd;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(int stepOrder) {
        this.stepOrder = stepOrder;
    }

    // helper per calcolare ritardo (positivo = ritardo, negativo = anticipo)
    // da commentare in quanto viene serializzato come attributo json, non so perché
    /*
    public long getStartDelayDays() {
        if (scheduledStart == null || actualStart == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(scheduledStart, actualStart);
    }

    public long getEndDelayDays() {
        if (scheduledEnd == null || actualEnd == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(scheduledEnd, actualEnd);
    }*/

    public String getAssignedEmployeeEmail() {
        return assignedEmployeeEmail;
    }

    public void setAssignedEmployeeEmail(String assignedEmployeeEmail) {
        this.assignedEmployeeEmail = assignedEmployeeEmail;
    }

    public String getAssignedEmployeeFullName() {
        return assignedEmployeeFullName;
    }

    public void setAssignedEmployeeFullName(String assignedEmployeeFullName) {
        this.assignedEmployeeFullName = assignedEmployeeFullName;
    }
}
