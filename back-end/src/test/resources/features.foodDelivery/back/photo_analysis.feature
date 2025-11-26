Feature: Analyse des photos de plats

  Scenario: L'IA identifie une image connue
    Given une image de plat "pizza_photo.jpg"
    When l'IA analyse la photo
    Then le plat identifié doit être "Pizza Margherita"
    And la catégorie doit être "MAIN_COURSE"
    And le type doit être "VEGETARIAN"

  Scenario: L'IA ne reconnaît pas une image inconnue
    Given une image de plat "plat_inconnu.jpg"
    When l'IA analyse la photo
    Then aucun plat ne doit être identifié
