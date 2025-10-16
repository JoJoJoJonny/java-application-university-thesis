package server_group.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

@Entity
@Table(name = "orders") // 'order' è parola riservata
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "model_name")
    private Model model;

    @Positive(message = "Quantity must be greater than 0")
    private int quantity;
    private LocalDate createDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate deadline;

    @ManyToOne
    @JoinColumn(name = "client_piva")
    private Client client;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Transient //significa che non viene salvato a database
    private OrderState state;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDate getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDate createDate) {
        this.createDate = createDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setState(OrderState state) {
        this.state = state;
    }

    /*
    //non va bene creare qui lo state in quanto non posso iniettare da spring
    public OrderState getState() {
        if (state == null) {
            switch (status) {
                case CREATED -> state = new OrderCreatedState();
                case IN_PRODUCTION -> state = new OrderInProductionState();
                case COMPLETED -> state = new OrderCompletedState();
                case CANCELLED -> state = new OrderCancelledState();
            }
        }
        return state;
    }
    */

    public OrderState getState() {
        return state;
    }
}

