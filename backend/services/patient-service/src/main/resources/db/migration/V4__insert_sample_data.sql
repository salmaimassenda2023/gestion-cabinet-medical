-- ============================================
-- V4__insert_sample_data.sql
-- ============================================
-- Insertion de données de test

-- Insertion de patients de test
INSERT INTO patients (cin, nom, prenom, date_naissance, sexe, telephone, email, adresse, type_mutuelle, numero_mutuelle, id_cabinet) VALUES
                                                                                                                                         ('AB123456', 'Alami', 'Mohammed', '1990-05-15', 'M', '0612345678', 'mohammed.alami@email.com', '123 Rue Hassan II, Casablanca', 'CNSS', 'CNSS123456', 1),
                                                                                                                                         ('CD789012', 'Bennani', 'Fatima', '1985-08-20', 'F', '0698765432', 'fatima.bennani@email.com', '45 Avenue Mohammed V, Rabat', 'CNOPS', 'CNOPS789012', 1),
                                                                                                                                         ('EF345678', 'El Amrani', 'Youssef', '1978-03-10', 'M', '0676543210', 'youssef.elamrani@email.com', '78 Boulevard Zerktouni, Marrakech', 'CNSS', 'CNSS345678', 1),
                                                                                                                                         ('GH901234', 'Idrissi', 'Amal', '1995-12-05', 'F', '0623456789', 'amal.idrissi@email.com', '12 Rue de la Liberté, Fès', 'CNOPS', 'CNOPS901234', 2),
                                                                                                                                         ('IJ567890', 'Tazi', 'Karim', '1982-07-22', 'M', '0654321098', 'karim.tazi@email.com', '56 Avenue des FAR, Tanger', 'CNSS', 'CNSS567890', 2),
                                                                                                                                         ('KL234567', 'Fassi', 'Nadia', '1992-11-18', 'F', '0687654321', 'nadia.fassi@email.com', '34 Rue Allal Ben Abdellah, Meknès', 'CNOPS', 'CNOPS234567', 2),
                                                                                                                                         ('MN890123', 'Berrada', 'Hassan', '1975-04-30', 'M', '0612987654', 'hassan.berrada@email.com', '89 Boulevard Mohammed VI, Agadir', 'CNSS', 'CNSS890123', 3),
                                                                                                                                         ('OP456789', 'Chraibi', 'Samira', '1988-09-14', 'F', '0698123456', 'samira.chraibi@email.com', '23 Rue Ibn Khaldoun, Oujda', 'CNOPS', 'CNOPS456789', 3);

-- Création automatique des dossiers médicaux pour chaque patient
INSERT INTO dossiers_medicaux (patient_id, antecedents_medicaux, antecedents_chirurgicaux, allergies, groupe_sanguin, remarques)
SELECT
    id,
    CASE
        WHEN id = 1 THEN 'Diabète de type 2, Hypertension artérielle'
        WHEN id = 2 THEN 'Asthme léger'
        WHEN id = 3 THEN 'Cholestérol élevé'
        WHEN id = 4 THEN 'Aucun antécédent majeur'
        WHEN id = 5 THEN 'Ulcère gastrique traité'
        WHEN id = 6 THEN 'Migraine chronique'
        WHEN id = 7 THEN 'Arthrose'
        WHEN id = 8 THEN 'Aucun antécédent'
        ELSE NULL
        END,
    CASE
        WHEN id = 1 THEN 'Appendicectomie (2015)'
        WHEN id = 3 THEN 'Hernie inguinale (2018)'
        WHEN id = 5 THEN 'Cholécystectomie (2020)'
        WHEN id = 7 THEN 'Prothèse du genou (2019)'
        ELSE NULL
        END,
    CASE
        WHEN id = 1 THEN 'Pénicilline, Fruits de mer'
        WHEN id = 2 THEN 'Pollen, Acariens'
        WHEN id = 4 THEN 'Aspirine'
        WHEN id = 6 THEN 'Lactose'
        ELSE NULL
        END,
    CASE
        WHEN id = 1 THEN 'O+'
        WHEN id = 2 THEN 'A+'
        WHEN id = 3 THEN 'B+'
        WHEN id = 4 THEN 'AB+'
        WHEN id = 5 THEN 'O-'
        WHEN id = 6 THEN 'A-'
        WHEN id = 7 THEN 'B-'
        WHEN id = 8 THEN 'AB-'
        ELSE NULL
        END,
    CASE
        WHEN id = 1 THEN 'Patient régulier, bon suivi du traitement'
        WHEN id = 2 THEN 'Suivi nécessaire tous les 6 mois'
        WHEN id = 5 THEN 'Contrôle post-opératoire réussi'
        WHEN id = 7 THEN 'Rééducation en cours'
        ELSE NULL
        END
FROM patients;