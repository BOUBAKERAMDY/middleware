# language: fr
Fonctionnalité: Gestion de la capacité des créneaux
  En tant que système
  Je veux gérer dynamiquement la capacité des créneaux horaires
  Afin que les commandes s'adaptent à la capacité disponible du restaurant

  Contexte:
    Étant donné un restaurant "Trattoria Roma" avec l'identifiant 1

  Scénario: Ajouter plusieurs créneaux horaires par demi-heure
    Quand le propriétaire ajoute les créneaux suivants:
      | créneau     | début | fin   | capacité |
      | 12:00-12:30 | 12:00 | 12:30 | 15       |
      | 12:30-13:00 | 12:30 | 13:00 | 15       |
      | 13:00-13:30 | 13:00 | 13:30 | 12       |
      | 13:30-14:00 | 13:30 | 14:00 | 10       |
    Alors tous les créneaux sont disponibles

  Scénario: Vérifier la disponibilité d'un créneau
    Étant donné un créneau "12:00-12:30" avec une capacité de 5
    Quand 3 commandes sont réservées pour ce créneau
    Alors le créneau "12:00-12:30" est toujours disponible
    Et le créneau a 2 places restantes

  Scénario: Créneau devient indisponible quand capacité atteinte
    Étant donné un créneau "18:00-18:30" avec une capacité de 2
    Quand 2 commandes sont réservées pour ce créneau
    Alors le créneau "18:00-18:30" n'est plus disponible
    Et aucune nouvelle commande ne peut être acceptée pour ce créneau

  Scénario: Augmenter la capacité d'un créneau populaire
    Étant donné un créneau "19:00-19:30" avec une capacité de 10
    Et 8 commandes sont déjà réservées pour ce créneau
    Quand le propriétaire augmente la capacité du créneau à 15
    Alors le créneau "19:00-19:30" est disponible
    Et 7 places supplémentaires sont disponibles

  Scénario: Réduire la capacité d'un créneau moins populaire
    Étant donné un créneau "14:00-14:30" avec une capacité de 20
    Et 5 commandes sont réservées pour ce créneau
    Quand le propriétaire réduit la capacité du créneau à 10
    Alors le créneau "14:00-14:30" est toujours disponible
    Et 5 places sont disponibles

  Scénario: Empêcher la réduction de capacité en dessous des réservations existantes
    Étant donné un créneau "20:00-20:30" avec une capacité de 10
    Et 8 commandes sont réservées pour ce créneau
    Quand le propriétaire tente de réduire la capacité du créneau à 5
    Alors la modification est acceptée
    Mais aucune nouvelle commande ne peut être prise

  Scénario: Distribution des commandes sur plusieurs créneaux
    Étant donné les créneaux suivants avec capacités limitées:
      | créneau     | capacité |
      | 12:00-12:30 | 3        |
      | 12:30-13:00 | 3        |
      | 13:00-13:30 | 3        |
    Quand 9 commandes sont créées
    Alors les commandes sont distribuées sur les 3 créneaux
    Et chaque créneau atteint sa capacité maximale

  Plan du Scénario: Validation de capacité pour différents créneaux
    Étant donné un créneau "<créneau>" avec une capacité de <capacité>
    Quand <commandes> commandes sont réservées
    Alors la disponibilité du créneau est <disponible>

    Exemples:
      | créneau     | capacité | commandes | disponible |
      | 12:00-12:30 | 10      | 5         | oui        |
      | 12:30-13:00 | 5       | 5         | non        |
      | 13:00-13:30 | 20      | 15        | oui        |
      | 19:00-19:30 | 3       | 2         | oui        |
      | 19:30-20:00 | 2       | 2         | non        |

  Scénario: Gestion de créneaux pendant les heures de pointe
    Étant donné les créneaux de déjeuner avec capacité élevée:
      | créneau     | capacité |
      | 12:00-12:30 | 30       |
      | 12:30-13:00 | 30       |
      | 13:00-13:30 | 25       |
    Et les créneaux hors pointe avec capacité normale:
      | créneau     | capacité |
      | 14:00-14:30 | 10       |
      | 14:30-15:00 | 10       |
    Quand 75 commandes sont passées pendant l'heure de déjeuner
    Alors toutes les commandes de déjeuner sont acceptées
    Et les créneaux hors pointe restent disponibles