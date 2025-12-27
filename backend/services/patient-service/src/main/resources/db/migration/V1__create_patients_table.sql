-- ============================================
-- V1__create_patients_table.sql
-- ============================================
-- Création de la table patients

CREATE TABLE patients (
                          id BIGSERIAL PRIMARY KEY,
                          cin VARCHAR(20) NOT NULL UNIQUE,
                          nom VARCHAR(255) NOT NULL,
                          prenom VARCHAR(255) NOT NULL,
                          date_naissance DATE NOT NULL,
                          sexe VARCHAR(1) NOT NULL CHECK (sexe IN ('M', 'F')),  -- CHAR(1) → VARCHAR(1)
                          telephone VARCHAR(20),
                          email VARCHAR(255),
                          adresse TEXT,
                          type_mutuelle VARCHAR(100),
                          numero_mutuelle VARCHAR(100),
                          id_cabinet BIGINT NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Création des index
CREATE INDEX idx_cin ON patients(cin);
CREATE INDEX idx_id_cabinet ON patients(id_cabinet);
CREATE INDEX idx_nom_prenom ON patients(nom, prenom);

-- Fonction trigger pour updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger pour mettre à jour automatiquement updated_at
CREATE TRIGGER update_patients_updated_at
    BEFORE UPDATE ON patients
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();