# language: fr
Fonctionnalité: Gestion des restaurants
  En tant que propriétaire de restaurant
  Je veux gérer mon menu, mes informations publiques, mon planning et ma capacité
  Afin que les clients ne puissent commander que les repas qui peuvent être réellement préparés et livrés

  Contexte:
    Étant donné un restaurant "Bella Italia" avec l'identifiant 1

  Scénario: Ajouter un repas au menu du restaurant
    Quand le propriétaire ajoute un repas "Pizza Margherita" à 12.50 euros avec:
      | catégorie     | MAIN_COURSE  |
      | type          | VEGETARIAN   |
      | topping       | Mozzarella   |
      | topping       | Tomato Sauce |
      | topping       | Basil        |
      | dietary_tag   | VEGETARIAN   |
    Alors le menu du restaurant contient 1 repas
    Et le repas "Pizza Margherita" est dans le menu

  Scénario: Supprimer un repas du menu
    Étant donné un repas "Burger" à 10.00 euros dans le menu
    Quand le propriétaire supprime le repas "Burger" du menu
    Alors le menu du restaurant contient 0 repas
    Et le repas "Burger" n'est pas dans le menu

  Scénario: Mettre à jour les informations publiques du restaurant
    Quand le propriétaire met à jour les informations publiques avec "Meilleur restaurant italien de la ville"
    Alors les informations publiques du restaurant sont "Meilleur restaurant italien de la ville"

  Scénario: Ajouter un créneau horaire avec capacité
    Quand le propriétaire ajoute un créneau "12:00-12:30" de 12:00 à 12:30 avec une capacité de 10
    Alors le créneau "12:00-12:30" est disponible

  Scénario: Ajouter un créneau avec capacité invalide
    Quand le propriétaire tente d'ajouter un créneau "12:00-12:30" de 12:00 à 12:30 avec une capacité de 0
    Alors une exception de planning est levée avec le message "Capacity must be positive"

  Scénario: Mettre à jour la capacité d'un créneau
    Étant donné un créneau "18:00-18:30" de 18:00 à 18:30 avec une capacité de 15
    Quand le propriétaire met à jour la capacité du créneau "18:00-18:30" à 20
    Alors le créneau "18:00-18:30" est disponible

  Scénario: Réserver des créneaux pour des commandes
    Étant donné un créneau "14:00-14:30" de 14:00 à 14:30 avec une capacité de 3
    Quand une commande est réservée pour le créneau "14:00-14:30"
    Et une autre commande est réservée pour le créneau "14:00-14:30"
    Alors le créneau "14:00-14:30" est toujours disponible

  Scénario: Dépassement de capacité d'un créneau
    Étant donné un créneau "20:00-20:30" de 20:00 à 20:30 avec une capacité de 2
    Et une commande est réservée pour le créneau "20:00-20:30"
    Et une autre commande est réservée pour le créneau "20:00-20:30"
    Quand une troisième commande tente de réserver le créneau "20:00-20:30"
    Alors une exception de capacité dépassée est levée

  Plan du Scénario: Ajouter plusieurs types de repas au menu
    Quand le propriétaire ajoute un repas "<nom>" à <prix> euros de catégorie "<categorie>" et type "<type>"
    Alors le repas "<nom>" est dans le menu
    Et le repas "<nom>" a une catégorie "<categorie>"

    Exemples:
      | nom                        | prix  | categorie    | type        |
      | Pizza Quattro Formaggi     | 15.00 | MAIN_COURSE  | VEGETARIAN  |
      | Spaghetti Bolognese        | 13.00 | MAIN_COURSE  | REGULAR     |
      | Tiramisu                   | 7.00  | DESSERT      | VEGETARIAN  |
      | Caesar Salad               | 9.00  | STARTER      | VEGETARIAN  |
      | Grilled Chicken            | 18.00 | MAIN_COURSE  | REGULAR     |

  Scénario: Configuration complète d'un restaurant
    Quand le propriétaire configure le restaurant avec:
      | informations | Cuisine italienne authentique |
    Et ajoute les repas suivants au menu:
      | nom                    | prix  | catégorie    | type       |
      | Pizza Margherita       | 12.00 | MAIN_COURSE  | VEGETARIAN |
      | Lasagna                | 14.00 | MAIN_COURSE  | VEGETARIAN |
      | Panna Cotta            | 6.50  | DESSERT      | VEGETARIAN |
    Et configure les créneaux horaires:
      | créneau       | début | fin   | capacité |
      | 12:00-12:30   | 12:00 | 12:30 | 20       |
      | 12:30-13:00   | 12:30 | 13:00 | 20       |
      | 19:00-19:30   | 19:00 | 19:30 | 25       |
    Alors le restaurant est complètement configuré avec 3 repas et 3 créneaux
