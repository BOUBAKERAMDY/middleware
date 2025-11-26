# API RESTAURANT

| Fonction | Requête |
|----------|---------|
|Ajouter un restaurant | POST /restaurants|	
|Modifier un restaurant | PUT /restaurants/{id}|
|Ajouter un repas |	POST /restaurants/{id}/menu/meals|	
|Retirer un repas |	DELETE /restaurants/{id}/menu/meals/{mealId}|	
|Mettre à jour un repas | PUT /restaurants/{id}/menu/meals/{mealId}|	
|Récupérer un repas par nom | GET /restaurants/{id}/menu/meals?name=...|	
|Ajouter un timeslot |	POST /restaurants/{id}/timeslots|
|Modifier la capacité |	PATCH /restaurants/{id}/timeslots/{slotId}/capacity|