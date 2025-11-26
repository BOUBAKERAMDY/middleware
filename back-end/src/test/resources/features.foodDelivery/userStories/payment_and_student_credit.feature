# language: fr
Fonctionnalité: Paiement et crédit étudiant
  En tant qu'utilisateur
  Je veux payer mes commandes avec différents moyens de paiement
  Afin de valider mes commandes

  Contexte:
    Étant donné un utilisateur enregistré "Sophie Martin" avec l'adresse "Campus A"
    Et un restaurant "Le Gourmet"
    Et le restaurant "Le Gourmet" propose le plat "Burger" à 10.00 euros
    Et un autre restaurant "Pizza Napoli"

  # [P1] Payment step required to validate order
  Scénario: Compléter le paiement standard pour valider la commande
    Étant donné une commande en cours pour l'utilisateur avec:
      | restaurant | Le Gourmet              |
      | plat       | Burger                  |
      | montant    | 10.00                   |
      | créneau    | 12:00-12:30             |
    Quand l'utilisateur sélectionne le mode de paiement "CARD"
    Et l'utilisateur est redirigé vers le service de paiement externe
    Et le paiement externe est confirmé avec succès
    Alors la commande est validée
    Et le statut de paiement est "PAID"

  Scénario: Le paiement est obligatoire pour valider une commande
    Étant donné une commande en cours pour l'utilisateur avec:
      | restaurant | Le Gourmet              |
      | plat       | Burger                  |
      | montant    | 10.00                   |
      | créneau    | 12:00-12:30             |
    Quand l'utilisateur tente de valider la commande sans payer
    Alors une erreur est levée indiquant "Le paiement est obligatoire"
    Et la commande n'est pas validée

  Scénario: Échec du paiement externe empêche la validation
    Étant donné une commande en cours pour l'utilisateur avec:
      | restaurant | Le Gourmet              |
      | plat       | Burger                  |
      | montant    | 10.00                   |
      | créneau    | 12:00-12:30             |
    Quand l'utilisateur sélectionne le mode de paiement "CARD"
    Et l'utilisateur est redirigé vers le service de paiement externe
    Et le paiement externe échoue
    Alors la commande n'est pas validée
    Et le statut de paiement est "FAILED"
    Et un message d'erreur est affiché "Le paiement a échoué"

  # [P2] Successful payment registers order in user account
  Scénario: Enregistrer la commande après un paiement réussi
    Étant donné une commande en cours pour l'utilisateur avec:
      | restaurant | Le Gourmet              |
      | plat       | Burger                  |
      | montant    | 10.00                   |
      | créneau    | 12:00-12:30             |
    Quand l'utilisateur complète le paiement avec succès
    Alors la commande est enregistrée dans le compte de l'utilisateur
    Et l'utilisateur peut consulter la commande dans son historique
    Et la commande a le statut "VALIDATED"

  Scénario: Consulter l'historique des commandes payées
    Étant donné que l'utilisateur a payé les commandes suivantes:
      | restaurant   | montant | date       | statut    |
      | Le Gourmet   | 10.00   | 2025-10-20 | VALIDATED |
      | Pizza Napoli | 15.00   | 2025-10-19 | VALIDATED |
    Quand l'utilisateur consulte son historique de commandes
    Alors l'utilisateur voit 2 commandes
    Et toutes les commandes ont le statut "VALIDATED"

  # [P3] Student credit mechanism
  Scénario: Étudiant reçoit une allocation initiale
    Étant donné un nouvel étudiant "Pierre Durand"
    Quand le compte étudiant est créé
    Alors l'étudiant a une allocation de 20.00 euros
    Et le solde disponible est de 20.00 euros

  Scénario: Payer avec le crédit étudiant
    Étant donné un étudiant "Pierre Durand" avec une allocation de 20.00 euros
    Et une commande en cours pour l'étudiant avec:
      | restaurant | Le Gourmet  |
      | plat       | Burger      |
      | montant    | 10.00       |
      | créneau    | 12:00-12:30 |
    Quand l'étudiant sélectionne le mode de paiement "STUDENT_CREDIT"
    Et l'étudiant confirme le paiement
    Alors le paiement est accepté
    Et le solde de l'allocation est réduit de 10.00 euros
    Et le solde restant est de 10.00 euros
    Et la commande est validée

  Scénario: Consulter le solde du crédit étudiant
    Étant donné un étudiant "Pierre Durand" avec une allocation de 20.00 euros
    Quand l'étudiant consulte son solde
    Alors le solde disponible affiché est de 20.00 euros

  Scénario: Payer partiellement avec le crédit étudiant
    Étant donné un étudiant "Marie Blanc" avec une allocation de 5.00 euros
    Et une commande en cours pour l'étudiant avec:
      | restaurant | Le Gourmet  |
      | plat       | Burger      |
      | montant    | 10.00       |
      | créneau    | 12:00-12:30 |
    Quand l'étudiant sélectionne le mode de paiement "STUDENT_CREDIT"
    Alors un message indique "Solde insuffisant, complément requis: 5.00 euros"
    Et l'étudiant peut compléter avec un autre mode de paiement

  Scénario: Refuser le paiement si crédit étudiant insuffisant
    Étant donné un étudiant "Luc Bernard" avec une allocation de 3.00 euros
    Et une commande en cours pour l'étudiant avec:
      | restaurant | Le Gourmet  |
      | plat       | Burger      |
      | montant    | 10.00       |
      | créneau    | 12:00-12:30 |
    Quand l'étudiant tente de payer uniquement avec "STUDENT_CREDIT"
    Alors une erreur est levée indiquant "Allocation insuffisante"
    Et le paiement est refusé
    Et la commande n'est pas validée

  Scénario: Historique des transactions du crédit étudiant
    Étant donné un étudiant "Alice Moreau" avec une allocation de 20.00 euros
    Et l'étudiant a payé les commandes suivantes avec le crédit étudiant:
      | restaurant   | montant | date       |
      | Le Gourmet   | 10.00   | 2025-10-20 |
      | Pizza Napoli | 8.00    | 2025-10-19 |
    Quand l'étudiant consulte l'historique de son crédit
    Alors l'étudiant voit 2 transactions
    Et le solde restant est de 2.00 euros

  Scénario: Utiliser tout le crédit étudiant disponible
    Étant donné un étudiant "Thomas Petit" avec une allocation de 20.00 euros
    Et une commande en cours pour l'étudiant avec:
      | restaurant | Le Gourmet  |
      | plat       | Burger      |
      | montant    | 20.00       |
      | créneau    | 12:00-12:30 |
    Quand l'étudiant paie avec le crédit étudiant
    Alors le paiement est accepté
    Et le solde de l'allocation est de 0.00 euro
    Et la commande est validée

  Scénario: Recharger l'allocation étudiante
    Étant donné un étudiant "Emma Roux" avec une allocation de 5.00 euros
    Quand l'allocation est rechargée de 20.00 euros
    Alors le solde de l'allocation est de 25.00 euros

  Scénario: Combiner crédit étudiant et autre mode de paiement
    Étant donné un étudiant "Hugo Blanc" avec une allocation de 8.00 euros
    Et une commande en cours pour l'étudiant avec:
      | restaurant | Le Gourmet  |
      | plat       | Burger      |
      | montant    | 15.00       |
      | créneau    | 12:00-12:30 |
    Quand l'étudiant utilise 8.00 euros du crédit étudiant
    Et l'étudiant paie le complément de 7.00 euros par carte bancaire
    Alors les deux paiements sont acceptés
    Et le solde de l'allocation est de 0.00 euro
    Et la commande est validée

