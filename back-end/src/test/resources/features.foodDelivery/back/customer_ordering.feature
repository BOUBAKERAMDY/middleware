# language: fr
Fonctionnalité: Commande client
  En tant que client
  Je veux ajouter des repas à ma commande
  Afin de personnaliser ma commande

  Contexte:
    Étant donné un restaurant "Le Gourmet" avec l'identifiant 1
    Et un client "John Doe" à l'adresse "123 Main St" avec une allocation de 100.00 euros
    Et le restaurant a les repas suivants dans son menu:
      | nom             | prix  | catégorie    | type       |
      | Classic Burger  | 12.50 | MAIN_COURSE  | REGULAR    |
      | French Fries    | 4.50  | SIDE_DISH    | VEGAN      |
      | Caesar Salad    | 9.00  | STARTER      | VEGETARIAN |

  Scénario: Client ajoute un repas à sa commande
    Étant donné une commande en cours pour le client
    Quand le client ajoute le repas "Classic Burger" à sa commande
    Alors la commande contient 1 repas
    Et le montant total de la commande est 12.50 euros

  Scénario: Client ajoute plusieurs repas à sa commande
    Étant donné une commande en cours pour le client
    Quand le client ajoute les repas suivants à sa commande:
      | nom             |
      | Classic Burger  |
      | French Fries    |
      | Caesar Salad    |
    Alors la commande contient 3 repas
    Et le montant total de la commande est 26.00 euros

  Scénario: Client tente d'ajouter un repas qui n'est pas au menu
    Étant donné une commande en cours pour le client
    Quand le client tente d'ajouter un repas "Sushi Roll" qui n'est pas au menu
    Alors une exception "meal not in menu" est levée

  Scénario: Client tente d'ajouter un repas sans commande en cours
    Quand le client tente d'ajouter un repas "Classic Burger" sans commande en cours
    Alors une exception "no current order" est levée

  Scénario: Plusieurs clients commandent avec gestion de capacité
    Étant donné un créneau "19:00-19:30" avec une capacité de 3
    Et le restaurant a un repas "Grilled Steak" à 25.00 euros dans son menu
    Et un client "Alice" crée une commande et ajoute "Grilled Steak"
    Et un client "Bob" crée une commande et ajoute "Grilled Steak"
    Quand un client "Charlie" crée une commande et ajoute "Grilled Steak"
    Alors les 3 commandes sont créées avec succès

  Scénario: Validation de commande avec repas du menu
    Étant donné une commande en cours pour le client
    Quand le client ajoute le repas "Classic Burger" à sa commande
    Et le client ajoute le repas "French Fries" à sa commande
    Alors la commande est valide
    Et tous les repas de la commande proviennent du menu du restaurant

  Plan du Scénario: Client avec différentes allocations budgétaires
    Étant donné un client "Client Test" avec une allocation de <budget> euros
    Et une commande en cours pour ce client
    Quand le client ajoute un repas à <prix> euros à sa commande
    Alors la commande a un montant de <prix> euros
    Et le client a une allocation suffisante: <suffisant>

    Exemples:
      | budget | prix  | suffisant |
      | 50.00  | 12.50 | oui       |
      | 20.00  | 12.50 | oui       |
      | 10.00  | 12.50 | non       |
      | 100.00 | 85.00 | oui       |
      | 5.00   | 15.00 | non       |
