#language: fr
Fonctionnalité: : Enregistrement des commandes
  En tant qu'étudiant
  Je veux pouvoir passer des commandes dans les restaurants du campus
  Afin de me faire livrer mes repas

  Scénario: Un étudiant passe une commande dans un restaurant du campus
    Et un client nommé "Charlie" situé à "Lyon" avec une allowance de "50.0"
    Et un restaurant nommé "Le Crous Gourmand" proposant un repas "Pâtes Bolognaises" à "7.5"
    Et une commande en cours pour ce client la
    Quand le client ajoute "Pâtes Bolognaises" à sa commande
    Et le restaurant enregistre la commande
    Alors la commande doit avoir le statut "PENDING"
    Et le montant total doit être de "7.5"
    Et la commande doit apparaître dans l'historique du client