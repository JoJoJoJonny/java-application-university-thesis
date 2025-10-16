package server_group.model;

import server_group.service.OrderProductionService;

public class OrderInProductionState implements OrderState{
    private final OrderProductionService productionService;

    public OrderInProductionState(OrderProductionService productionService) {
        this.productionService = productionService;
    }

    @Override
    public void startProduction(Order order) {
        throw new IllegalStateException("The order is already in production!");
    }

    @Override
    public void complete(Order order) {
        // Elimina tutti i process step execution
        productionService.deleteAllExecutions(order);

        // Aggiorna stato ordine
        order.setStatus(OrderStatus.COMPLETED);
        order.setState(new OrderCompletedState());
        System.out.println("Order completed!");
    }

    @Override
    public void cancel(Order order) {
        // Elimina tutti i process step execution
        productionService.deleteAllExecutions(order);

        // Aggiorna stato ordine
        order.setStatus(OrderStatus.CANCELLED);
        order.setState(new OrderCancelledState());
        System.out.println("Order cancelled!");
    }

    @Override
    public String getName() {
        return "IN_PRODUCTION";
    }
}
