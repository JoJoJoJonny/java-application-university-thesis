package server_group.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class ProcessStepExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    private String machineryName;

    private int stepIndex;

    private LocalDate scheduledStart;
    private LocalDate scheduledEnd;

    private LocalDate actualStart;
    private LocalDate actualEnd;

    @ManyToOne
    @JoinColumn(name = "assigned_employee_id")
    private CustomUser assignedEmployee;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
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

    public CustomUser getAssignedEmployee() {
        return assignedEmployee;
    }

    public void setAssignedEmployee(CustomUser assignedEmployee) {
        this.assignedEmployee = assignedEmployee;
    }
}