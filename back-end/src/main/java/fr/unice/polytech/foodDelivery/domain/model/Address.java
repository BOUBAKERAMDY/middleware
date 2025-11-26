package fr.unice.polytech.foodDelivery.domain.model;

import fr.unice.polytech.foodDelivery.domain.ENUM.Suffix;

public class Address {
    private String name;
    private int number;
    private Suffix suffix;
    private String road;
    private int postalCode;
    private String city;

    public Address(String name, int number, Suffix suffix, int postalCode, String road, String city) {
        this.name = name;
        this.number = number;
        this.suffix = suffix;
        this.postalCode = postalCode;
        this.road = road;
        this.city = city;
    }

    @Override
    public String toString() {
        return "Name: " + name + "," +
                "Address{" +
                "number=" + number +
                ", suffix=" + suffix +
                ", road='" + road + '\'' +
                ", postalCode=" + postalCode +
                ", city='" + city + '\'' +
                '}';
    }
}