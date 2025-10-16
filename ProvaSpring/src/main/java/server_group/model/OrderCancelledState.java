package server_group.model;

public class OrderCancelledState implements OrderState{
    @Override
    public void startProduction(Order order) {
        throw new IllegalStateException("The order is cancelled!");
    }

    @Override
    public void complete(Order order) {
        throw new IllegalStateException("The order is cancelled!");
    }

    @Override
    public void cancel(Order order) {
        throw new IllegalStateException("The order is already cancelled!");
    }

    @Override
    public String getName() {
        return "CANCELLED";
    }
}
