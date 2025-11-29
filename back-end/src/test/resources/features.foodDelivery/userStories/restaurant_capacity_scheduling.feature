# language: fr
Fonctionnalité: Gestion des horaires et capacités du restaurant
  En tant que gestionnaire de restaurant
  Je veux mettre à jour les horaires d'ouverture et les capacités par demi-heure
  Afin de gérer efficacement le nombre de commandes

  Contexte:
    Étant donné un restaurant "Le Gourmet" géré par "Jeanne Dubois"

  # [R5] Update opening hours and capacity by half-hour slots
  Scénario: Définir les capacités par créneaux de demi-heure
    Quand le gestionnaire définit les créneaux de capacité suivants:
      | heure         | capacité |
      | 11:00-11:30   | 5        |
      | 11:30-12:00   | 10       |
      | 12:00-12:30   | 30       |
      | 12:30-13:00   | 30       |
      | 13:00-13:30   | 30       |
      | 13:30-14:00   | 30       |
    Alors le restaurant a 6 créneaux de capacité configurés
    Et le créneau "11:00-11:30" a une capacité de 5 commandes
    Et le créneau "11:30-12:00" a une capacité de 10 commandes
    Et le créneau "12:00-12:30" a une capacité de 30 commandes

  Scénario: Mettre à jour la capacité d'un créneau existant
    Étant donné que le restaurant a les créneaux suivants:
      | heure         | capacité |
      | 11:00-11:30   | 5        |
      | 11:30-12:00   | 10       |
    Quand le gestionnaire met à jour le créneau "11:30-12:00" avec une capacité de 15
    Alors le créneau "11:30-12:00" a une capacité de 15 commandes
    Et le créneau "11:00-11:30" reste à 5 commandes

  Scénario: Configurer une capacité élevée pour les heures de pointe
    Quand le gestionnaire définit les créneaux de capacité suivants:
      | heure         | capacité |
      | 12:00-12:30   | 30       |
      | 12:30-13:00   | 30       |
      | 13:00-13:30   | 30       |
      | 13:30-14:00   | 30       |
    Alors tous les créneaux entre "12:00" et "14:00" ont une capacité de 30 commandes

  Scénario: Ajouter de nouveaux créneaux d'ouverture
    Étant donné que le restaurant a les créneaux suivants:
      | heure         | capacité |
      | 12:00-12:30   | 20       |
      | 12:30-13:00   | 20       |
    Quand le gestionnaire ajoute les créneaux suivants:
      | heure         | capacité |
      | 11:30-12:00   | 10       |
      | 13:00-13:30   | 15       |
    Alors le restaurant a 4 créneaux de capacité configurés
    Et le créneau "11:30-12:00" a une capacité de 10 commandes
    Et le créneau "13:00-13:30" a une capacité de 15 commandes

  Scénario: Supprimer un créneau d'ouverture
    Étant donné que le restaurant a les créneaux suivants:
      | heure         | capacité |
      | 11:00-11:30   | 5        |
      | 11:30-12:00   | 10       |
      | 12:00-12:30   | 20       |
    Quand le gestionnaire supprime le créneau "11:00-11:30"
    Alors le restaurant a 2 créneaux de capacité configurés
    Et le restaurant n'a pas de créneau "11:00-11:30"

  Scénario: Vérifier la capacité restante d'un créneau
    Étant donné que le restaurant a les créneaux suivants:
      | heure         | capacité |
      | 12:00-12:30   | 30       |
    Et le restaurant a 18 commandes validées pour le créneau "12:00-12:30"
    Quand le gestionnaire consulte la capacité restante du créneau "12:00-12:30"
    Alors la capacité restante est de 12 commandes

  Scénario: Adapter les capacités selon les jours de la semaine
    Quand le gestionnaire définit les créneaux pour "Lundi":
      | heure         | capacité |
      | 12:00-12:30   | 20       |
      | 12:30-13:00   | 20       |
    Et le gestionnaire définit les créneaux pour "Vendredi":
      | heure         | capacité |
      | 12:00-12:30   | 35       |
      | 12:30-13:00   | 35       |
    Alors le créneau "12:00-12:30" du "Lundi" a une capacité de 20 commandes
    Et le créneau "12:00-12:30" du "Vendredi" a une capacité de 35 commandes

  Scénario: Empêcher la réduction de capacité en dessous du nombre de commandes validées
    Étant donné que le restaurant a les créneaux suivants:
      | heure         | capacité |
      | 12:00-12:30   | 30       |
    Et le restaurant a 25 commandes validées pour le créneau "12:00-12:30"
    Quand le gestionnaire tente de mettre à jour le créneau "12:00-12:30" avec une capacité de 20
    Alors une erreur est levée indiquant "Impossible de réduire la capacité en dessous du nombre de commandes validées"
    Et le créneau "12:00-12:30" garde sa capacité de 30 commandes

  Scénario: Configurer des horaires d'ouverture étendus
    Quand le gestionnaire définit les créneaux de capacité suivants:
      | heure         | capacité |
      | 10:00-10:30   | 3        |
      | 10:30-11:00   | 5        |
      | 11:00-11:30   | 8        |
      | 11:30-12:00   | 12       |
      | 12:00-12:30   | 25       |
      | 12:30-13:00   | 30       |
      | 13:00-13:30   | 25       |
      | 13:30-14:00   | 20       |
      | 14:00-14:30   | 10       |
    Alors le restaurant a 9 créneaux de capacité configurés
    Et la capacité totale journalière est de 138 commandes

