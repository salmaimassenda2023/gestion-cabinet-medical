CREATE TABLE cabinets (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    specialite VARCHAR(255) NOT NULL,
    adresse VARCHAR(255) NOT NULL,
    tel VARCHAR(20) NOT NULL,
    logo TEXT,
    medecin_id BIGINT,
    actif BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE services_consultation (
    id_service BIGSERIAL PRIMARY KEY,
    nom_service VARCHAR(255) NOT NULL,
    description TEXT,
    prix DOUBLE PRECISION NOT NULL,
    actif BOOLEAN DEFAULT TRUE,
    obligatoire BOOLEAN DEFAULT FALSE,
    cabinet_id BIGINT,
    CONSTRAINT fk_cabinet FOREIGN KEY (cabinet_id) REFERENCES cabinets(id) ON DELETE CASCADE
);
