package fr.unice.polytech.foodDelivery.domain.exception;

public class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }
}
