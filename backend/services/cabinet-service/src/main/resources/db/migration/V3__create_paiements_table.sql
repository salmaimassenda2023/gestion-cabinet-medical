CREATE TABLE paiements_abonnement (
    id_paiement BIGSERIAL PRIMARY KEY,
    date_paiement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    montant DOUBLE PRECISION NOT NULL,
    statut VARCHAR(50) NOT NULL,
    abonnement_id BIGINT,
    CONSTRAINT fk_abonnement_paiement FOREIGN KEY (abonnement_id) REFERENCES abonnements_cabinet(id_abonnement) ON DELETE CASCADE
);
