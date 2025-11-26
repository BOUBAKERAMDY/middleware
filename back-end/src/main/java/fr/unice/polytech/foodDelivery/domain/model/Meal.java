package fr.unice.polytech.foodDelivery.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import fr.unice.polytech.foodDelivery.domain.ENUM.DiateryPreference;
import fr.unice.polytech.foodDelivery.domain.ENUM.DishType;
import fr.unice.polytech.foodDelivery.domain.ENUM.MealCategory;
import fr.unice.polytech.foodDelivery.domain.ENUM.MealType;
import lombok.Getter;
import lombok.Setter;

@JsonDeserialize(builder = Meal.Builder.class)
public class Meal {
    @Getter
    private UUID mealId;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private double price;
    @Getter
    @Setter
    private String description;
    @Getter
    @Setter
    private MealCategory category;
    @Getter
    @Setter
    private MealType type;
    @Setter
    private List<String> toppings;
    @Setter
    private List<DiateryPreference> dietaryTags;
    @Getter
    private DishType dishType;


    private Meal(Builder builder) {
        this.mealId = builder.mealId != null ? builder.mealId : UUID.randomUUID();
        this.name = builder.name;
        this.price = builder.price;
        this.description = builder.description;
        this.category = builder.category;
        this.type = builder.type;
        this.toppings = builder.toppings;
        this.dietaryTags = builder.dietaryTags;
    }

    public List<String> getToppings() {
        return toppings != null ? new ArrayList<>(toppings) : new ArrayList<>();
    }

    public List<DiateryPreference> getDietaryTags() {
        return dietaryTags != null ? new ArrayList<>(dietaryTags) : new ArrayList<>();
    }

    public void displayMealInfo() {
        System.out.println("Meal ID: " + mealId);
        System.out.println("Name: " + name);
        System.out.println("Price: $" + price);
        System.out.println("Description: " + description);
        System.out.println("Category: " + category);
        System.out.println("Type: " + type);
        System.out.println("Toppings: " + String.join(", ", toppings));
        System.out.print("Dietary Tags: ");
        if (dietaryTags.isEmpty()) {
            System.out.println("None");
        } else {
            List<String> tags = new ArrayList<>();
            for (DiateryPreference tag : dietaryTags) {
                tags.add(tag.name());
            }
            System.out.println(String.join(", ", tags));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Meal))
            return false;
        Meal meal = (Meal) o;
        return this.mealId.equals(meal.mealId);
    }

    @Override
    public int hashCode() {
        return mealId != null ? mealId.hashCode() : 0;
    }

    @JsonPOJOBuilder(withPrefix = "set")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder{
        private UUID mealId;
        private String name;
        private double price;
        private String description;
        private MealCategory category;
        private MealType type;
        private List<String> toppings = new ArrayList<>();
        private List<DiateryPreference> dietaryTags = new ArrayList<>();

        @JsonCreator
        public Builder(@JsonProperty("name") String name, @JsonProperty("price") double price) {
            this.name = name;
            this.price = price;
        }

        public Builder mealId() {
            this.mealId = UUID.randomUUID();
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder category(MealCategory category) {
            this.category = category;
            return this;
        }

        public Builder type(MealType type) {
            this.type = type;
            return this;
        }

        public Builder addTopping(String topping) {
            this.toppings.add(topping);
            return this;
        }

        public Builder toppings(List<String> toppings) {
            this.toppings = new ArrayList<>(toppings);
            return this;
        }

        public Builder addDietaryTag(DiateryPreference tag) {
            this.dietaryTags.add(tag);
            return this;
        }

        public Builder dietaryTags(List<DiateryPreference> tags) {
            this.dietaryTags = new ArrayList<>(tags);
            return this;
        }

        public Meal build() {
            return new Meal(this);
        }
    }
}
