package server_group.model;

import server_group.service.OrderProductionService;

import java.time.LocalDate;

public class OrderCreatedState implements OrderState{
    private final OrderProductionService productionService;

    public OrderCreatedState(OrderProductionService productionService) {
        this.productionService = productionService;
    }

    @Override
    public void startProduction(Order order) {
        // scheduling
        productionService.startProduction(order);

        //va messo dopo in modo che venga cambiato lo stato SOLO se va a buon fire lo scheduling
        order.setStatus(OrderStatus.IN_PRODUCTION);
        order.setStartDate(LocalDate.now());
        order.setState(new OrderInProductionState(productionService));
        System.out.println("Order set in production!");
    }

    @Override
    public void complete(Order order) {
        throw new IllegalStateException("You cannot complete an order that is not in production!");
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
        return "CREATED";
    }
}
