INSERT INTO cabinets (nom, specialite, adresse, tel, medecin_id, actif) 
VALUES ('Cabinet Dr. Alami', 'Médecine Générale', '123 Rue Mohammed V, Casablanca', '+212 522 123456', 1, true);

INSERT INTO services_consultation (nom_service, description, prix, actif, obligatoire, cabinet_id)
VALUES 
('Consultation générale', 'Consultation standard', 200.00, true, true, 1),
('ECG', 'Electrocardiogramme', 150.00, true, false, 1);

INSERT INTO abonnements_cabinet (date_debut, date_fin, statut, montant, type_periode, cabinet_id)
VALUES (CURRENT_DATE, CURRENT_DATE + INTERVAL '1 month', 'ACTIF', 500.00, 'MENSUEL', 1);
