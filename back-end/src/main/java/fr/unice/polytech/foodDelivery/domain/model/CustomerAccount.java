package fr.unice.polytech.foodDelivery.domain.model;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

public class CustomerAccount implements OrderObserveur {
    @Getter
    private final UUID customerId;
    @Getter
    private String name;
    @Getter
    private String location;
    @Getter
    @Setter
    private double allowance;

    public CustomerAccount(String name, String location, double allowance) {
        this.customerId = UUID.randomUUID();
        this.name = name;
        this.location = location;
        this.allowance = allowance;
    }

    public CustomerAccount(UUID customerId, String name, String location, double allowance) {
        this.customerId = customerId;
        this.name = name;
        this.location = location;
        this.allowance = allowance;
    }

    public void notifyOrderRegistered() {
    }

    @Override
    public void onOrderStatusChange(Order order) {
        System.out.println("Customer " + this.name + " notified: Order " + order.getOrderId() + " status changed to "
                + order.getStatus());
    }
}
