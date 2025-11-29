#language: fr
Fonctionnalité: : Paiement des commandes
  En tant que client
  Je veux pouvoir payer mes commandes avec mon allowance
  Afin de finaliser mes achats

  Scénario: Paiement réussi avec allowance suffisante
  Et un client nommé "Alice" situé à "Nice" avec une allowance de "100.0"
  Et un restaurant nommé "Burger King" proposant un repas "Burger" à "10.0"
  Quand le client ajoute "Burger" à sa commande
  Et le restaurant enregistre la commande
  Et le client choisit de payer avec son allowance
  Et le client paie sa commande
  Alors la commande doit être marquée comme payée
  Et le solde du client doit être réduit de "10.0"
  Et la commande doit avoir le statut "PAID"

  Scénario: Paiement échoué avec allowance insuffisante
  Et un client nommé "Bob" situé à "Paris" avec une allowance de "5.0"
  Et un restaurant nommé "Pizza Hut" proposant un repas "Pizza" à "15.0"
  Quand le client ajoute "Pizza" à sa commande
  Et le restaurant enregistre la commande
  Et le client tente de payer la commande avec son allowance
  Alors le paiement doit échouer
  Et la commande doit avoir le statut "PENDING"
  Et l'allowance du client doit être de "5.0"