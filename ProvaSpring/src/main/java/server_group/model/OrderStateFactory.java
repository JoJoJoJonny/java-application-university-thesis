package server_group.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server_group.service.OrderProductionService;

@Component
public class OrderStateFactory {

    private final OrderProductionService productionService;

    public OrderStateFactory(OrderProductionService productionService) {
        this.productionService = productionService;
    }

    public OrderState createState(Order order) {
        return switch (order.getStatus()) {
            case CREATED -> new OrderCreatedState(productionService);
            case IN_PRODUCTION -> new OrderInProductionState(productionService);
            case COMPLETED -> new OrderCompletedState();
            case CANCELLED -> new OrderCancelledState();
        };
    }
}
