package fr.unice.polytech.foodDelivery.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import fr.unice.polytech.foodDelivery.domain.ENUM.DiateryPreference;
import fr.unice.polytech.foodDelivery.domain.ENUM.MealCategory;
import fr.unice.polytech.foodDelivery.domain.ENUM.MealType;
import fr.unice.polytech.foodDelivery.domain.model.Meal;

public class MockAIPhotoAnalysisService {
        private static MockAIPhotoAnalysisService instance = new MockAIPhotoAnalysisService();

        public static MockAIPhotoAnalysisService getInstance() {
                return instance;
        }

        private Map<String, Meal> trainingDatabase;

        private MockAIPhotoAnalysisService() {
                initializeTrainingDatabase();
        }

        /**
         * [AI1a] Base de données d'entraînement diversifiée couvrant différentes
         * cuisines
         */
        private void initializeTrainingDatabase() {
                trainingDatabase = new HashMap<>();

                // Cuisine italienne
                trainingDatabase.put("pizza", new Meal.Builder("Pizza Margherita", 8.5)
                                .category(MealCategory.MAIN_COURSE)
                                .type(MealType.VEGETARIAN)
                                .toppings(Arrays.asList("Tomate", "Mozzarella", "Basilic"))
                                .dietaryTags(Arrays.asList(DiateryPreference.GLUTEN_FREE))
                                .build());
                trainingDatabase.put("pasta", new Meal.Builder("Pâtes Carbonara", 9.0)
                                .category(MealCategory.MAIN_COURSE)
                                .type(MealType.REGULAR)
                                .toppings(Arrays.asList("Pâtes", "Œuf", "Lardons", "Parmesan"))
                                .dietaryTags(Arrays.asList(DiateryPreference.LACTOSE_FREE))
                                .build());

                // Cuisine japonaise
                trainingDatabase.put("sushi", new Meal.Builder("Sushi Assortiment", 12.0)
                                .category(MealCategory.MAIN_COURSE)
                                .type(MealType.REGULAR)
                                .toppings(Arrays.asList("Riz", "Poisson cru", "Algue"))
                                .dietaryTags(Arrays.asList(DiateryPreference.FROZEN_INSIDE))
                                .build());
                trainingDatabase.put("ramen", new Meal.Builder("Ramen au Poulet", 10.5)
                                .category(MealCategory.MAIN_COURSE)
                                .type(MealType.REGULAR)
                                .toppings(Arrays.asList("Nouilles", "Bouillon", "Poulet", "Œuf"))
                                .dietaryTags(Arrays.asList(DiateryPreference.PEANUTS_TRACE))
                                .build());

                // Cuisine indienne
                trainingDatabase.put("curry", new Meal.Builder("Curry de Légumes", 9.5)
                                .category(MealCategory.MAIN_COURSE)
                                .type(MealType.VEGAN)
                                .toppings(Arrays.asList("Légumes", "Épices", "Riz"))
                                .dietaryTags(Arrays.asList(DiateryPreference.VEGAN))
                                .build());
                trainingDatabase.put("biryani", new Meal.Builder("Biryani au Poulet", 11.0)
                                .category(MealCategory.MAIN_COURSE)
                                .type(MealType.REGULAR)
                                .toppings(Arrays.asList("Riz", "Poulet", "Épices"))
                                .dietaryTags(Arrays.asList(DiateryPreference.GLUTEN_FREE))
                                .build());

                // Cuisine française
                trainingDatabase.put("croissant", new Meal.Builder("Croissant au Beurre", 2.5)
                                .category(MealCategory.BREAKFAST)
                                .type(MealType.VEGETARIAN)
                                .toppings(Arrays.asList("Pâte feuilletée", "Beurre"))
                                .dietaryTags(Arrays.asList(DiateryPreference.LACTOSE_FREE))
                                .build());
                trainingDatabase.put("ratatouille", new Meal.Builder("Ratatouille Provençale", 8.0)
                                .category(MealCategory.MAIN_COURSE)
                                .type(MealType.VEGAN)
                                .toppings(Arrays.asList("Légumes", "Herbes de Provence"))
                                .dietaryTags(Arrays.asList(DiateryPreference.VEGAN))
                                .build());

                // Cuisine orientale
                trainingDatabase.put("kebab", new Meal.Builder("Kebab d'Agneau", 7.5)
                                .category(MealCategory.MAIN_COURSE)
                                .type(MealType.REGULAR)
                                .toppings(Arrays.asList("Agneau", "Pain pita", "Légumes"))
                                .dietaryTags(Arrays.asList(DiateryPreference.PEANUTS_TRACE))
                                .build());
                trainingDatabase.put("falafel", new Meal.Builder("Falafel Végétarien", 6.5)
                                .category(MealCategory.MAIN_COURSE)
                                .type(MealType.VEGAN)
                                .toppings(Arrays.asList("Pois chiches", "Épices", "Pain pita"))
                                .dietaryTags(Arrays.asList(DiateryPreference.VEGAN))
                                .build());

                // Cuisine américaine
                trainingDatabase.put("burger", new Meal.Builder("Burger Classique", 9.0)
                                .category(MealCategory.MAIN_COURSE)
                                .type(MealType.REGULAR)
                                .toppings(Arrays.asList("Boeuf", "Fromage", "Laitue", "Tomate"))
                                .dietaryTags(Arrays.asList(DiateryPreference.PEANUTS_TRACE))
                                .build());

                // Salades
                trainingDatabase.put("salad", new Meal.Builder("Salade César", 7.5)
                                .category(MealCategory.STARTER)
                                .type(MealType.VEGETARIAN)
                                .toppings(Arrays.asList("Laitue", "Croûtons", "Parmesan", "Poulet"))
                                .dietaryTags(Arrays.asList(DiateryPreference.GLUTEN_FREE))
                                .build());
        }

        public Meal analyzePhoto(String imagePath) {
                System.out.println("\n[AI1] Analyse de photo par IA pour: " + imagePath);
                System.out.println("[AI1a] Utilisation de modèle entraîné sur données culinaires diversifiées");

                String fileName = imagePath.toLowerCase();

                for (Map.Entry<String, Meal> entry : trainingDatabase.entrySet()) {
                        if (fileName.contains(entry.getKey())) {
                                Meal data = entry.getValue();
                                System.out.println(" Plat identifié: " + data.getName());
                                return checkResultIA(data);
                        }
                }

                System.out.println(" Plat non identifié - hors de la base d'entraînement");
                return null;
        }

        public Meal checkResultIA(Meal meal) {
                meal.displayMealInfo();
                System.out.println("Voulez vous modifier certaines infos ?");
                String res = "n"; // ScannerUtil.getScanner().nextLine();
                while (!res.equals("n")) {
                        System.out.println(
                                        "Quelle info voulez vous modifier ? (name/category/type/price/toppings/dietaryTags)");
                        String info = "n"; // ScannerUtil.getScanner().nextLine();
                        System.out.println("Nouvelle valeur ?");
                        String newValue = "n"; // ScannerUtil.getScanner().nextLine();
                        switch (info) {
                                case "name":
                                        meal.setName(newValue);
                                        break;
                                case "category":
                                        meal.setCategory(MealCategory.valueOf(newValue.toUpperCase()));
                                        break;
                                case "type":
                                        meal.setType(MealType.valueOf(newValue.toUpperCase()));
                                        break;
                                case "price":
                                        meal.setPrice(Double.parseDouble(newValue));
                                        break;
                                case "toppings":
                                        meal.setToppings(Arrays.asList(newValue.split(",")));
                                        break;
                                case "dietaryTags":
                                        meal.setDietaryTags(Arrays
                                                        .asList(DiateryPreference.valueOf(newValue.toUpperCase())));
                                        break;
                                default:
                                        System.out.println("Info inconnue.");
                        }
                        System.out.println("Voulez vous modifier certaines infos ?");
                        res = "n"; // ScannerUtil.getScanner().nextLine();
                }

                return meal;
        }
}