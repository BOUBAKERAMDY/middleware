package fr.unice.polytech.foodDelivery.domain.ENUM;

public enum OrderStatus {
    PENDING("Pending"),
    VALIDATED("Validated"),
    PREPARING("Preparing"),
    READY("Ready"),
    PAID("Paid"),
    FAILED("Failed"),
    DELIVERING("Delivering"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
