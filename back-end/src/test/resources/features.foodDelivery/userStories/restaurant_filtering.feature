# language: fr
Fonctionnalité: Filtrage des restaurants
  En tant qu'utilisateur internet
  Je veux filtrer les restaurants selon différents critères
  Afin de trouver facilement le restaurant qui correspond à mes besoins

  Contexte:
    Étant donné les restaurants suivants:
      | nom                  | type       | cuisineType    |
      | CROUS Central        | CROUS      | FRENCH         |
      | Pizza Napoli         | RESTAURANT | ITALIAN        |
      | Greek Food Truck     | FOOD_TRUCK | MEDITERRANEAN  |
      | Bio et Local         | RESTAURANT | FRENCH         |

  # [C2] Filter by availability
  Scénario: Filtrer les restaurants par disponibilité
    Étant donné que "CROUS Central" peut préparer une commande à "12:00"
    Et que "Pizza Napoli" ne peut pas préparer une commande à "12:00"
    Et que "Greek Food Truck" peut préparer une commande à "12:00"
    Quand l'utilisateur filtre les restaurants disponibles à "12:00"
    Alors l'utilisateur voit 2 restaurants
    Et la liste contient "CROUS Central"
    Et la liste contient "Greek Food Truck"

  # [C2] Filter by dietary preferences
  Scénario: Filtrer les restaurants avec des plats végétariens
    Étant donné que "CROUS Central" propose des plats:
      | nom           | type        |
      | Salade verte  | VEGETARIAN  |
    Et que "Pizza Napoli" propose des plats:
      | nom                | type     |
      | Pizza 4 fromages   | REGULAR  |
    Et que "Bio et Local" propose des plats:
      | nom              | type        |
      | Buddha Bowl      | VEGAN       |
      | Tarte légumes    | VEGETARIAN  |
    Quand l'utilisateur filtre les restaurants avec des plats "VEGETARIAN"
    Alors l'utilisateur voit 2 restaurants
    Et la liste contient "CROUS Central"
    Et la liste contient "Bio et Local"

  Scénario: Filtrer les restaurants avec des plats végans
    Étant donné que "Bio et Local" propose des plats:
      | nom              | type   |
      | Buddha Bowl      | VEGAN  |
    Et que "CROUS Central" propose des plats:
      | nom           | type     |
      | Steak frites  | REGULAR  |
    Quand l'utilisateur filtre les restaurants avec des plats "VEGAN"
    Alors l'utilisateur voit 1 restaurant
    Et la liste contient "Bio et Local"

  Scénario: Filtrer les restaurants par gamme de prix
    Étant donné que "CROUS Central" propose des plats:
      | nom           | prix  |
      | Menu étudiant | 3.30  |
      | Plat du jour  | 5.50  |
    Et que "Pizza Napoli" propose des plats:
      | nom                | prix   |
      | Pizza Margherita   | 12.00  |
      | Pizza 4 fromages   | 15.00  |
    Et que "Greek Food Truck" propose des plats:
      | nom        | prix  |
      | Souvlaki   | 8.50  |
      | Gyros      | 9.00  |
    Quand l'utilisateur filtre les restaurants avec des prix entre 0.00 et 10.00 euros
    Alors l'utilisateur voit 2 restaurants
    Et la liste contient "CROUS Central"
    Et la liste contient "Greek Food Truck"

  # [C2] Filter by cuisine type
  Scénario: Filtrer les restaurants par type de cuisine
    Quand l'utilisateur filtre les restaurants par type de cuisine "ITALIAN"
    Alors l'utilisateur voit 1 restaurant
    Et la liste contient "Pizza Napoli"

  Scénario: Filtrer les restaurants par cuisine française
    Quand l'utilisateur filtre les restaurants par type de cuisine "FRENCH"
    Alors l'utilisateur voit 2 restaurants
    Et la liste contient "CROUS Central"
    Et la liste contient "Bio et Local"

  # [C2] Filter by establishment type
  Scénario: Filtrer les restaurants par type d'établissement CROUS
    Quand l'utilisateur filtre les restaurants par type d'établissement "CROUS"
    Alors l'utilisateur voit 1 restaurant
    Et la liste contient "CROUS Central"

  Scénario: Filtrer les restaurants par type d'établissement Food Truck
    Quand l'utilisateur filtre les restaurants par type d'établissement "FOOD_TRUCK"
    Alors l'utilisateur voit 1 restaurant
    Et la liste contient "Greek Food Truck"

  # Combined filters
  Scénario: Combiner plusieurs filtres
    Étant donné que "Bio et Local" est de type "RESTAURANT" avec cuisine "FRENCH"
    Et que "Bio et Local" propose des plats:
      | nom           | type        | prix  |
      | Salade bio    | VEGETARIAN  | 8.50  |
    Et que "CROUS Central" est de type "CROUS" avec cuisine "FRENCH"
    Et que "CROUS Central" propose des plats:
      | nom           | type     | prix  |
      | Menu complet  | REGULAR  | 3.30  |
    Quand l'utilisateur filtre les restaurants avec:
      | critère        | valeur      |
      | cuisineType    | FRENCH      |
      | type           | RESTAURANT  |
      | dietaryPref    | VEGETARIAN  |
    Alors l'utilisateur voit 1 restaurant
    Et la liste contient "Bio et Local"

