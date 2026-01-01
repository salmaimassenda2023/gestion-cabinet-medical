CREATE TABLE rendezvous (
                            id BIGSERIAL PRIMARY KEY,

                            id_patient BIGINT NOT NULL,
                            id_medecin BIGINT NOT NULL,

                            date_rdv DATE NOT NULL,
                            heure_rdv TIME NOT NULL,
                            motif VARCHAR(50) NOT NULL,
                            statut VARCHAR(50) NOT NULL,

                            ordre_passage INTEGER NULL,
                            heure_arrivee TIMESTAMP NULL,

                            date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            date_modification TIMESTAMP NULL,

                            CONSTRAINT chk_motif CHECK (motif IN ('CONSULTATION', 'CONTROLE', 'URGENCE', 'AUTRE')),
                            CONSTRAINT chk_statut CHECK (statut IN ('PLANIFIE', 'CONFIRME', 'PRESENT', 'EN_CONSULTATION', 'TERMINE', 'ANNULE'))
);