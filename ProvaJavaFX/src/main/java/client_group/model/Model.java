package client_group.model;

import java.util.List;

public class Model {
    private String name;
    private double price;
    private Raw raw;
    private List<ProcessStep> processSteps;

    public Long getId() {
        return raw != null ? raw.getId() : null;
    }
    @Override
    public String toString() {
        return name;
    }


    // Getter e Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Raw getRaw() { return raw; }
    public void setRaw(Raw raw) { this.raw = raw; }

    public List<ProcessStep> getProcessSteps() {
        return processSteps;
    }

    public void setProcessSteps(List<ProcessStep> processSteps) {
        this.processSteps = processSteps;
    }

}
