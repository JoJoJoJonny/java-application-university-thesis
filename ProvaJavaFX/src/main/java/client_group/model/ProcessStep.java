package client_group.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Duration;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessStep {
    private Long id;
    private int stepOrder;
    private Duration duration;
    private String semifinishedName;
    private Machinery machinery;

    // Getter e Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getStepOrder() { return stepOrder; }
    public void setStepOrder(int stepOrder) { this.stepOrder = stepOrder; }

    public Duration getDuration() { return duration; }
    public void setDuration(Duration duration) { this.duration = duration; }

    public String getSemifinishedName() { return semifinishedName; }
    public void setSemifinishedName(String semifinishedName) { this.semifinishedName = semifinishedName; }

    public Machinery getMachinery() { return machinery; }
    public void setMachinery(Machinery machinery) { this.machinery = machinery; }
}
