package fr.unice.polytech.foodDelivery.domain.exception;

public class NoCurrentOrderException extends RuntimeException {
    public NoCurrentOrderException() {
        super("No current order exists. Please create an order first.");
    }

    public NoCurrentOrderException(String message) {
        super(message);
    }
}