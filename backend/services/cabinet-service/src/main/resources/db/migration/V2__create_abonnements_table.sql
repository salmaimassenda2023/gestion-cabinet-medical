CREATE TABLE abonnements_cabinet (
    id_abonnement BIGSERIAL PRIMARY KEY,
    date_debut TIMESTAMP NOT NULL,
    date_fin TIMESTAMP NOT NULL,
    statut VARCHAR(50) ,
    montant DOUBLE PRECISION NOT NULL,
    type_periode VARCHAR(50) NOT NULL,
    cabinet_id BIGINT UNIQUE,
    CONSTRAINT fk_cabinet_abonnement FOREIGN KEY (cabinet_id) REFERENCES cabinets(id) ON DELETE CASCADE
);
