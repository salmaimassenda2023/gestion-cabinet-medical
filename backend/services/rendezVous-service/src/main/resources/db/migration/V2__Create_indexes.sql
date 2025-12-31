-- ========================================
-- V2__Create_indexes.sql
-- ========================================
-- Description: Création des index pour optimiser les performances

-- Index pour rechercher les RDV d'un patient
CREATE INDEX idx_patient ON rendezvous(id_patient);

-- Index composite pour rechercher les RDV d'un médecin à une date
CREATE INDEX idx_medecin_date ON rendezvous(id_medecin, date_rdv);

-- Index composite pour filtrer par médecin et statut (liste d'attente)
CREATE INDEX idx_medecin_statut ON rendezvous(id_medecin, statut);

-- Index pour optimiser les recherches par date et heure (vérification disponibilité)
CREATE INDEX idx_medecin_date_heure ON rendezvous(id_medecin, date_rdv, heure_rdv);

-- Index pour la liste d'attente (ordre de passage)
CREATE INDEX idx_liste_attente ON rendezvous(id_medecin, date_rdv, statut, ordre_passage);