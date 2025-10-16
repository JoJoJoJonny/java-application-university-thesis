package client_group.dto;

import java.time.Duration;
import java.util.List;

public class ModelWithStepsDTO {
    private String name;
    private double price;
    private RawDTO raw;
    private List<ProcessStepDTO> processSteps;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public RawDTO getRaw() {
        return raw;
    }

    public void setRaw(RawDTO raw) {
        this.raw = raw;
    }

    public List<ProcessStepDTO> getProcessSteps() {
        return processSteps;
    }

    public void setProcessSteps(List<ProcessStepDTO> processSteps) {
        this.processSteps = processSteps;
    }

    public static class RawDTO {
        private String material;
        private String shape;
        private String size;

        public String getMaterial() {
            return material;
        }

        public void setMaterial(String material) {
            this.material = material;
        }

        public String getShape() {
            return shape;
        }

        public void setShape(String shape) {
            this.shape = shape;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }
    }

    public static class ProcessStepDTO {
        private Long id;
        private int stepOrder;
        private Duration duration;
        private String semifinishedName;
        private MachineryDTO machinery;

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

        public MachineryDTO getMachinery() {
            return machinery;
        }

        public void setMachinery(MachineryDTO machinery) {
            this.machinery = machinery;
        }
    }

    public static class MachineryDTO {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
