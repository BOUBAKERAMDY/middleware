package fr.unice.polytech.foodDelivery.domain.model;

public interface OrderObserveur {
    void onOrderStatusChange(Order order);
}
