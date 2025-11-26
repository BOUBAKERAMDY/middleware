# language: fr
Fonctionnalité: Navigation et consultation des plats par les utilisateurs internet
  En tant qu'utilisateur internet
  Je veux consulter les plats des différents restaurants du campus
  Afin de découvrir l'offre disponible

  # [C1] Any internet user can browse dishes from different campus restaurants
  Scénario: Consultation des plats de plusieurs restaurants
    Étant donné les restaurants suivants sur le campus:
      | nom                  | type       |
      | CROUS Central        | CROUS      |
      | Pizza Truck          | FOOD_TRUCK |
      | Restaurant Le Jardin | RESTAURANT |
    Et le restaurant "CROUS Central" propose les plats suivants:
      | nom                | prix  | catégorie    |
      | Salade César       | 5.50  | STARTER      |
      | Poulet grillé      | 8.00  | MAIN_COURSE  |
    Et le restaurant "Pizza Truck" propose les plats suivants:
      | nom                | prix  | catégorie    |
      | Pizza Margherita   | 9.00  | MAIN_COURSE  |
      | Tiramisu           | 4.50  | DESSERT      |
    Quand l'utilisateur consulte les restaurants disponibles
    Alors l'utilisateur voit 3 restaurants
    Et l'utilisateur peut voir tous les plats de chaque restaurant

  Scénario: Consultation des détails d'un plat
    Étant donné un restaurant "CROUS Central" avec le plat suivant:
      | nom           | prix  | catégorie    | description                    |
      | Burger végé   | 7.50  | MAIN_COURSE  | Burger végétarien aux légumes  |
    Quand l'utilisateur consulte les détails du plat "Burger végé"
    Alors l'utilisateur voit le nom "Burger végé"
    Et l'utilisateur voit le prix 7.50 euros
    Et l'utilisateur voit la description "Burger végétarien aux légumes"

