package server_group.model;

import jakarta.persistence.*;

@Entity
public class Model {
    @Id
    private String name;

    @ManyToOne
    @JoinColumn(name = "id_raw")
    private Raw raw;

    private Double price;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Raw getRaw() {
        return raw;
    }

    public void setRaw(Raw raw) {
        this.raw = raw;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
