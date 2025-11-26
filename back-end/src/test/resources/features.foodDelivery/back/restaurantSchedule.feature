#language: fr
Fonctionnalité: : Gestion des horaires des restaurants
  En tant que restaurant
  Je veux pouvoir modifier mes horaires d'ouverture
  Afin d'informer les clients de mes disponibilités

  Scénario: Le restaurant modifie ses heures d'ouverture avec succès
  Et un restaurant "La Belle Table" avec un repas "Salade César" au prix de "12.5"
  Quand le restaurant modifie ses horaires d'ouverture pour "MONDAY" de "08:00" à "18:00"
  Alors les horaires du restaurant doivent indiquer qu'il est ouvert le "MONDAY" de "08:00" à "18:00"