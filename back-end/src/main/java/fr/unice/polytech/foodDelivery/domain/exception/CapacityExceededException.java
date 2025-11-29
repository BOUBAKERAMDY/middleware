package fr.unice.polytech.foodDelivery.domain.exception;

public class CapacityExceededException extends RuntimeException {
    private final String timeSlot;

    public CapacityExceededException(String timeSlot) {
        super(String.format("Capacity exceeded for time slot: %s", timeSlot));
        this.timeSlot = timeSlot;
    }

    public String getTimeSlot() {
        return timeSlot;
    }
}

