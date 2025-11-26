package fr.unice.polytech.foodDelivery.model;

import org.junit.Test;

import fr.unice.polytech.foodDelivery.domain.model.Address;

public class AddressTest {

    @Test
    public void testToString() {
        Address address = new Address("Home", 123, null, 6000, "Main Street", "Nice");
        String expected = "Name: Home,Address{number=123, suffix=null, road='Main Street', postalCode=6000, city='Nice'}";
        assert address.toString().equals(expected);
    }
}
