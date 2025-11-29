package fr.unice.polytech.foodDelivery.domain.model;

import lombok.Getter;
import lombok.Setter;
import fr.unice.polytech.foodDelivery.domain.ENUM.PaymentMethod;
import fr.unice.polytech.foodDelivery.domain.ENUM.PaymentStatus;

import java.util.UUID;

public class Payment {
    @Getter
    private final UUID id;
    @Getter
    private Order order;
    @Getter
    private double amount;
    @Getter
    private PaymentMethod method; // ex: CARD, ALLOWANCE
    @Getter @Setter
    private PaymentStatus status;

    public Payment(Order order, double amount, PaymentMethod method) {
        this.id = UUID.randomUUID();
        this.order = order;
        this.amount = amount;
        this.method = method;
        this.status = PaymentStatus.PENDING;
    }
}
