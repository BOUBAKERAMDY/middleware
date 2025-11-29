package fr.unice.polytech.foodDelivery.domain.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class Menu {
    @Getter
    private List<Meal> meals;

    public Menu() {
        this.meals = new ArrayList<>();
    }

    public Menu(List<Meal> meals) {
        this.meals = new ArrayList<>(meals);
    }

    public void addMeal(Meal meal) {
        if (!meals.contains(meal)) {
            meals.add(meal);
        }
    }

    public void removeMeal(Meal meal) {
        meals.remove(meal);
    }

    public boolean containsMeal(Meal meal) {
        return meals.contains(meal);
    }

    public boolean containsMealByName(String mealName) {
        return meals.stream().anyMatch(m -> m.getName().equals(mealName));
    }


    public Meal getMealByName(String name) {
        return meals.stream()
                .filter(m -> m.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Menu))
            return false;

        Menu menu = (Menu) o;

        return meals.equals(menu.meals);
    }

    @Override
    public int hashCode() {
        return meals.hashCode();
    }
}
