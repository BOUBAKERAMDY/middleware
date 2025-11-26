package fr.unice.polytech.foodDelivery.domain.exception;
import lombok.Getter;

@Getter
public class InsufficientAllowanceException extends RuntimeException {
    private final double required;
    private final double available;

    public InsufficientAllowanceException(double required, double available) {
        super(String.format("Insufficient allowance: required=%.2f, available=%.2f", required, available));
        this.required = required;
        this.available = available;
    }
}