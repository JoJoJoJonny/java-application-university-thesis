package server_group.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;

import java.time.Duration;

@Entity
public class ProcessStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //
    private Long id;
    @Positive
    private int stepOrder;

    @ManyToOne
    @JoinColumn(name = "modelName", referencedColumnName = "name" )
    private Model model;

    @ManyToOne
    @JoinColumn(name = "machinery_id")
    private Machinery machinery;

    private Duration duration;
    private String semifinishedName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(int stepOrder) {
        this.stepOrder = stepOrder;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Machinery getMachinery() {
        return machinery;
    }

    public void setMachinery(Machinery machinery) {
        this.machinery = machinery;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public String getSemifinishedName() {
        return semifinishedName;
    }

    public void setSemifinishedName(String semifinishedName) {
        this.semifinishedName = semifinishedName;
    }
}

