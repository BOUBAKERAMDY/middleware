# language: fr
Fonctionnalité: Création de commande et sélection de livraison
  En tant qu'utilisateur enregistré
  Je veux créer une commande et sélectionner un créneau de livraison
  Afin de recevoir mes plats à l'heure souhaitée

  Contexte:
    Étant donné un utilisateur enregistré "Marie Dupont" avec l'adresse "Campus A - Bâtiment 1"
    Et un restaurant "Le Gourmet" enregistré dans le système
    Et le restaurant "Le Gourmet" a les créneaux de capacité suivants:
      | heure         | capacité |
      | 11:00-11:30   | 5        |
      | 11:30-12:00   | 10       |
      | 12:00-12:30   | 15       |
      | 12:30-13:00   | 15       |

  # [C5] Create order with restaurant and delivery location selection
  Scénario: Créer une commande avec sélection du restaurant
    Étant donné que l'utilisateur a enregistré les adresses de livraison suivantes:
      | adresse                    |
      | Campus A - Bâtiment 1      |
      | Campus B - Bibliothèque    |
    Quand l'utilisateur crée une nouvelle commande
    Et l'utilisateur sélectionne le restaurant "Le Gourmet"
    Alors la commande est créée avec le restaurant "Le Gourmet"

  Scénario: Sélectionner une adresse de livraison pré-enregistrée
    Étant donné que l'utilisateur a enregistré les adresses de livraison suivantes:
      | adresse                    |
      | Campus A - Bâtiment 1      |
      | Campus B - Bibliothèque    |
    Et une commande en cours pour l'utilisateur au restaurant "Le Gourmet"
    Quand l'utilisateur sélectionne l'adresse de livraison "Campus B - Bibliothèque"
    Alors l'adresse de livraison de la commande est "Campus B - Bibliothèque"

  # [C5] Visualize delivery dates
  Scénario: Visualiser les créneaux de livraison disponibles
    Étant donné une commande en cours pour l'utilisateur au restaurant "Le Gourmet"
    Et le restaurant a 2 commandes validées pour le créneau "12:00-12:30"
    Quand l'utilisateur consulte les créneaux de livraison disponibles
    Alors l'utilisateur voit les créneaux suivants disponibles:
      | heure         | places_restantes |
      | 11:00-11:30   | 5                |
      | 11:30-12:00   | 10               |
      | 12:00-12:30   | 13               |
      | 12:30-13:00   | 15               |

  Scénario: Les créneaux complets ne sont pas proposés
    Étant donné une commande en cours pour l'utilisateur au restaurant "Le Gourmet"
    Et le restaurant a 5 commandes validées pour le créneau "11:00-11:30"
    Quand l'utilisateur consulte les créneaux de livraison disponibles
    Alors l'utilisateur ne voit pas le créneau "11:00-11:30"
    Et l'utilisateur voit le créneau "11:30-12:00" disponible

  # [C6] Delivery dates change when adding items
  Scénario: Les créneaux disponibles changent après ajout d'articles
    Étant donné une commande en cours pour l'utilisateur au restaurant "Le Gourmet"
    Et le restaurant "Le Gourmet" propose le plat "Burger" à 10.00 euros
    Et le restaurant a 14 commandes validées pour le créneau "12:00-12:30"
    Et l'utilisateur voit le créneau "12:00-12:30" disponible avec 1 place
    Quand l'utilisateur ajoute le plat "Burger" à sa commande
    Et une autre commande est validée pour le créneau "12:00-12:30"
    Et l'utilisateur consulte à nouveau les créneaux disponibles
    Alors l'utilisateur ne voit plus le créneau "12:00-12:30"

  Scénario: Ajout de plusieurs articles met à jour les créneaux dynamiquement
    Étant donné une commande en cours pour l'utilisateur au restaurant "Le Gourmet"
    Et le restaurant "Le Gourmet" propose les plats suivants:
      | nom         | prix  |
      | Burger      | 10.00 |
      | Frites      | 3.50  |
      | Dessert     | 4.00  |
    Et le restaurant a 13 commandes validées pour le créneau "12:00-12:30"
    Quand l'utilisateur ajoute le plat "Burger" à sa commande
    Alors l'utilisateur voit le créneau "12:00-12:30" avec 2 places restantes
    Quand une autre commande est validée pour le créneau "12:00-12:30"
    Alors l'utilisateur voit le créneau "12:00-12:30" avec 1 place restante
    Quand l'utilisateur ajoute le plat "Frites" à sa commande
    Alors les créneaux disponibles sont recalculés
    Et l'utilisateur voit toujours le créneau "12:00-12:30" tant qu'il reste de la place

  # [C7] Validate order before payment
  Scénario: Valider la commande avec sélection du créneau avant paiement
    Étant donné une commande en cours pour l'utilisateur au restaurant "Le Gourmet"
    Et le restaurant "Le Gourmet" propose le plat "Burger" à 10.00 euros
    Et l'utilisateur a ajouté le plat "Burger" à sa commande
    Et l'adresse de livraison est "Campus A - Bâtiment 1"
    Quand l'utilisateur sélectionne le créneau de livraison "12:00-12:30"
    Et l'utilisateur valide la commande
    Alors la commande est en attente de paiement
    Et le créneau de livraison est "12:00-12:30"

  Scénario: Impossible de procéder au paiement sans sélection de créneau
    Étant donné une commande en cours pour l'utilisateur au restaurant "Le Gourmet"
    Et le restaurant "Le Gourmet" propose le plat "Burger" à 10.00 euros
    Et l'utilisateur a ajouté le plat "Burger" à sa commande
    Et l'utilisateur n'a pas sélectionné de créneau de livraison
    Quand l'utilisateur tente de valider la commande
    Alors une erreur est levée indiquant "Le créneau de livraison doit être sélectionné"
    Et la commande n'est pas validée

  Scénario: Sélectionner un créneau qui devient indisponible
    Étant donné une commande en cours pour l'utilisateur au restaurant "Le Gourmet"
    Et le restaurant "Le Gourmet" propose le plat "Burger" à 10.00 euros
    Et l'utilisateur a ajouté le plat "Burger" à sa commande
    Et le restaurant a 14 commandes validées pour le créneau "12:00-12:30"
    Quand une autre commande est validée pour le créneau "12:00-12:30"
    Et l'utilisateur tente de sélectionner le créneau "12:00-12:30"
    Alors une erreur est levée indiquant "Le créneau sélectionné n'est plus disponible"

