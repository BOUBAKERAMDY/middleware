# language: fr
Fonctionnalité: Gestion des plats par le restaurant
  En tant que gestionnaire de restaurant
  Je veux enregistrer et mettre à jour les plats de mon menu
  Afin de proposer une offre complète et à jour

  Contexte:
    Étant donné un restaurant "Le Gourmet" géré par "Jean Martin"

  # [R2] Register and update dishes with name and description
  Scénario: Enregistrer un nouveau plat avec nom et description
    Quand le gestionnaire crée un nouveau plat avec:
      | nom         | Burger Classique                          |
      | description | Burger maison avec steak et crudités      |
      | prix        | 10.50                                     |
      | catégorie   | MAIN_COURSE                               |
    Alors le plat "Burger Classique" est ajouté au menu
    Et le plat a la description "Burger maison avec steak et crudités"
    Et le plat coûte 10.50 euros

  Scénario: Mettre à jour la description d'un plat existant
    Étant donné que le restaurant a le plat suivant:
      | nom         | Burger Classique                     |
      | description | Burger simple                        |
      | prix        | 10.50                                |
      | catégorie   | MAIN_COURSE                          |
    Quand le gestionnaire met à jour le plat "Burger Classique" avec:
      | description | Burger maison avec steak haché 100% bœuf et crudités fraîches |
    Alors le plat "Burger Classique" a la nouvelle description
    Et le prix reste 10.50 euros

  # [R2] Dishes must belong to a general category
  Scénario: Enregistrer des plats avec différentes catégories
    Quand le gestionnaire crée les plats suivants:
      | nom              | prix  | catégorie    | description              |
      | Salade César     | 7.50  | STARTER      | Salade romaine et poulet |
      | Steak frites     | 15.00 | MAIN_COURSE  | Steak grillé avec frites |
      | Tarte au citron  | 5.00  | DESSERT      | Tarte meringuée          |
      | Coca-Cola        | 2.50  | BAVERAGE        | Boisson gazeuse          |
    Alors le menu contient 4 plats
    Et le plat "Salade César" est dans la catégorie "STARTER"
    Et le plat "Steak frites" est dans la catégorie "MAIN_COURSE"
    Et le plat "Tarte au citron" est dans la catégorie "DESSERT"
    Et le plat "Coca-Cola" est dans la catégorie "BAVERAGE"

  # [R2] Dishes may have optional specific types
  Scénario: Ajouter un type spécifique optionnel à un plat
    Quand le gestionnaire crée un nouveau plat avec:
      | nom         | Spaghetti Carbonara      |
      | description | Pâtes à la carbonara     |
      | prix        | 12.00                    |
      | catégorie   | MAIN_COURSE              |
      | type        | PASTA                    |
    Alors le plat "Spaghetti Carbonara" a le type spécifique "PASTA"

  Scénario: Créer des plats avec différents types spécifiques
    Quand le gestionnaire crée les plats suivants:
      | nom                | prix  | catégorie    | type       |
      | Pizza Margherita   | 11.00 | MAIN_COURSE  | PIZZA      |
      | Escalope milanaise | 14.00 | MAIN_COURSE  | MEAT       |
      | Glace vanille      | 4.00  | DESSERT      | ICE_CREAM  |
      | Tiramisu           | 5.50  | DESSERT      | CAKE       |
    Alors le plat "Pizza Margherita" a le type spécifique "PIZZA"
    Et le plat "Escalope milanaise" a le type spécifique "MEAT"
    Et le plat "Glace vanille" a le type spécifique "ICE_CREAM"
    Et le plat "Tiramisu" a le type spécifique "CAKE"

  Scénario: Créer un plat sans type spécifique
    Quand le gestionnaire crée un nouveau plat avec:
      | nom         | Plat du jour        |
      | description | Plat surprise       |
      | prix        | 9.50                |
      | catégorie   | MAIN_COURSE         |
    Alors le plat "Plat du jour" n'a pas de type spécifique

  # [R2] Dishes may offer additional paid toppings
  Scénario: Ajouter des suppléments payants à un plat
    Étant donné que le restaurant a le plat suivant:
      | nom         | Burger Classique    |
      | prix        | 10.50               |
      | catégorie   | MAIN_COURSE         |
    Quand le gestionnaire ajoute les suppléments suivants au plat "Burger Classique":
      | nom              | prix  |
      | Fromage cheddar  | 1.00  |
      | Bacon            | 1.50  |
      | Oignon caramélisé| 0.80  |
    Alors le plat "Burger Classique" a 3 suppléments disponibles
    Et le supplément "Fromage cheddar" coûte 1.00 euro
    Et le supplément "Bacon" coûte 1.50 euros

  Scénario: Commander un plat avec des suppléments
    Étant donné que le restaurant a le plat suivant:
      | nom         | Pizza Margherita    |
      | prix        | 11.00               |
      | catégorie   | MAIN_COURSE         |
    Et le plat a les suppléments suivants:
      | nom           | prix  |
      | Extra fromage | 1.50  |
      | Olives        | 1.00  |
      | Champignons   | 1.20  |
    Quand un client commande le plat "Pizza Margherita" avec les suppléments:
      | Extra fromage |
      | Champignons   |
    Alors le prix total du plat est 13.70 euros

  # [R2] Dishes may be tagged with dietary information
  Scénario: Ajouter des informations diététiques à un plat
    Quand le gestionnaire crée un nouveau plat avec:
      | nom         | Salade quinoa bio        |
      | description | Salade de quinoa         |
      | prix        | 8.50                     |
      | catégorie   | MAIN_COURSE              |
    Et ajoute les tags diététiques suivants:
      | GLUTEN_FREE |
      | VEGAN       |
      | ORGANIC     |
    Alors le plat "Salade quinoa bio" a le tag "GLUTEN_FREE"
    Et le plat "Salade quinoa bio" a le tag "VEGAN"
    Et le plat "Salade quinoa bio" a le tag "ORGANIC"

  Scénario: Ajouter des informations de composition et d'allergènes
    Quand le gestionnaire crée un nouveau plat avec:
      | nom         | Brownie chocolat         |
      | description | Brownies aux pépites     |
      | prix        | 4.50                     |
      | catégorie   | DESSERT                  |
    Et ajoute les tags de composition suivants:
      | CONTAINS_FROZEN_PRODUCTS    |
      | MAY_CONTAIN_PEANUT_TRACES   |
    Alors le plat "Brownie chocolat" a le tag "CONTAINS_FROZEN_PRODUCTS"
    Et le plat "Brownie chocolat" a le tag "MAY_CONTAIN_PEANUT_TRACES"

  Scénario: Mettre à jour les tags diététiques d'un plat
    Étant donné que le restaurant a le plat suivant:
      | nom         | Buddha Bowl     |
      | prix        | 9.00            |
      | catégorie   | MAIN_COURSE     |
    Et le plat a les tags suivants:
      | VEGETARIAN |
    Quand le gestionnaire met à jour les tags du plat "Buddha Bowl" avec:
      | VEGAN       |
      | GLUTEN_FREE |
      | ORGANIC     |
    Alors le plat "Buddha Bowl" a 3 tags diététiques
    Et le plat "Buddha Bowl" a le tag "VEGAN"
    Et le plat "Buddha Bowl" a le tag "GLUTEN_FREE"
    Et le plat "Buddha Bowl" a le tag "ORGANIC"

  Scénario: Supprimer un plat du menu
    Étant donné que le restaurant a les plats suivants:
      | nom              | prix  | catégorie    |
      | Burger Classique | 10.50 | MAIN_COURSE  |
      | Salade César     | 7.50  | STARTER      |
    Quand le gestionnaire supprime le plat "Burger Classique"
    Alors le menu contient 1 plat
    Et le menu ne contient pas "Burger Classique"

