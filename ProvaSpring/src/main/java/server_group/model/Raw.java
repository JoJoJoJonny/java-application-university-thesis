package server_group.model;

import jakarta.persistence.*;

@Entity
public class Raw {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String shape;
    private String material;
    private String size;
    private String thickness;
    private String castingNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getThickness() {
        return thickness;
    }

    public void setThickness(String thickness) {
        this.thickness = thickness;
    }

    public String getCastingNumber() {
        return castingNumber;
    }

    public void setCastingNumber(String castingNumber) {
        this.castingNumber = castingNumber;
    }
}

