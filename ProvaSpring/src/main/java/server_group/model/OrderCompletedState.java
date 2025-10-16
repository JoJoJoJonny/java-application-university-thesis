package server_group.model;

public class OrderCompletedState implements OrderState{
    @Override
    public void startProduction(Order order) {
        throw new IllegalStateException("The order is already completed!");
    }

    @Override
    public void complete(Order order) {
        throw new IllegalStateException("The order is already completed!");
    }

    @Override
    public void cancel(Order order) {
        throw new IllegalStateException("The order is already completed!");
    }

    @Override
    public String getName() {
        return "COMPLETED";
    }
}
