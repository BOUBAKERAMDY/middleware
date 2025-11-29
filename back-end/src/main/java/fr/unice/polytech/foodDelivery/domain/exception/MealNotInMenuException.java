package fr.unice.polytech.foodDelivery.domain.exception;
import java.util.UUID;

public class MealNotInMenuException extends RuntimeException {
    private final String mealName;
    private final UUID restaurantId;

    public MealNotInMenuException(String mealName, UUID restaurantId) {
        // Use %s for UUID (toString) to avoid IllegalFormatConversionException
        super(String.format("Meal '%s' is not in restaurant menu (ID: %s)", mealName, restaurantId));
        this.mealName = mealName;
        this.restaurantId = restaurantId;
    }

    public String getMealName() {
        return mealName;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }
}
