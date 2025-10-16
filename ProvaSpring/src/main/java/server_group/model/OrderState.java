package server_group.model;

public interface OrderState {
    void startProduction(Order order);
    void complete(Order order);
    void cancel(Order order);
    String getName();
}
