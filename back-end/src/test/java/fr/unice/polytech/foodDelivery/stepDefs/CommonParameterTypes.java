package fr.unice.polytech.foodDelivery.stepDefs;

import io.cucumber.java.ParameterType;
import java.time.LocalTime;

/**
 * Shared parameter types for all Cucumber step definitions
 * This avoids duplicate parameter type registrations
 */
public class CommonParameterTypes {

    @ParameterType("\\d+\\.\\d+")
    public Double prix(String value) {
        return Double.parseDouble(value);
    }

    @ParameterType("\\d{2}:\\d{2}")
    public LocalTime heure(String value) {
        return LocalTime.parse(value);
    }
}

