package fr.unice.polytech.foodDelivery.domain.exception;

import java.util.UUID;

public class NoSuchRestaurantException extends RuntimeException {
    public NoSuchRestaurantException(UUID id) {
        super("No restaurant matches in the database with the UUID:"+id);
    }
}