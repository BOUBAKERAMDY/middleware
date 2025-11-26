# language: fr
Fonctionnalité: Processus complet de commande
  En tant que client
  Je veux pouvoir passer une commande complète du début à la fin
  Afin de recevoir mes repas commandés

  Scénario: Commande réussie avec allowance suffisante
    Étant donné un client nommé "Alice" situé à "Nice" avec une allowance de "50.0"
    Et un restaurant nommé "Le Gourmet" avec le menu suivant:
      | plat            | prix  |
      | Pizza Margherita | 12.50 |
      | Salade César     | 8.00  |
      | Tiramisu         | 6.00  |
    Et le restaurant a les créneaux horaires suivants:
      | créneau     | jour    | début | fin   | capacité |
      | dejeuner_1  | MONDAY  | 12:00 | 12:30 | 10       |
      | dejeuner_2  | MONDAY  | 12:30 | 13:00 | 10       |
    Quand le client crée une nouvelle commande
    Et ajoute les plats suivants à sa commande:
      | plat            | quantité |
      | Pizza Margherita | 1        |
      | Salade César     | 1        |
    Et sélectionne le créneau horaire "dejeuner_1"
    Et le client paie la commande avec son allowance
    Alors la commande est confirmée
    Et le montant total de la commande est de "20.50"
    Et l'allowance du client est réduite à "29.50"
    Et la commande apparaît dans l'historique du client
    Et le restaurant est notifié de la nouvelle commande
    Et le créneau horaire "dejeuner_1" a sa capacité réduite de 1

  Scénario: Commande échouée avec allowance insuffisante
    Étant donné un client nommé "Bob" situé à "Paris" avec une allowance de "15.0"
    Et un restaurant nommé "Pizza Express" avec le menu suivant:
      | plat            | prix  |
      | Pizza Royale    | 18.00 |
      | Boisson         | 3.00  |
    Et le restaurant a les créneaux horaires suivants:
      | créneau     | jour    | début | fin   | capacité |
      | soir_1      | MONDAY  | 19:00 | 19:30 | 5        |
    Quand le client crée une nouvelle commande
    Et ajoute les plats suivants à sa commande:
      | plat         | quantité |
      | Pizza Royale | 1        |
      | Boisson      | 1        |
    Et sélectionne le créneau horaire "soir_1"
    Et le client tente de payer la commande avec son allowance
    Alors le paiement échoue
    Et la commande n'est pas confirmée
    Et le montant total de la commande est de "21.00"
    Et l'allowance du client reste à "15.0"
    Et le créneau horaire "soir_1" garde sa capacité initiale

  Scénario: Commande avec plusieurs articles
    Étant donné un client nommé "Charlie" situé à "Lyon" avec une allowance de "100.0"
    Et un restaurant nommé "Buffet Asiatique" avec le menu suivant:
      | plat            | prix |
      | Noodles         | 9.00 |
      | Riz Cantonais   | 7.50 |
      | Rouleaux Printemps | 5.00 |
      | Thé Vert        | 3.00 |
    Et le restaurant a les créneaux horaires suivants:
      | créneau     | jour    | début | fin   | capacité |
      | midi_1      | TUESDAY | 12:00 | 12:30 | 15       |
    Quand le client crée une nouvelle commande
    Et ajoute les plats suivants à sa commande:
      | plat              | quantité |
      | Noodles           | 2        |
      | Riz Cantonais     | 1        |
      | Rouleaux Printemps | 3       |
      | Thé Vert          | 2        |
    Et sélectionne le créneau horaire "midi_1"
    Et le client paie la commande avec son allowance
    Alors la commande est confirmée
    Et le montant total de la commande est de "46.50"
    Et l'allowance du client est réduite à "53.50"
    Et la commande apparaît dans l'historique du client