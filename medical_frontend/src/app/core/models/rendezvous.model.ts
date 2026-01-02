export enum MotifRendezVous {
    CONSULTATION = 'CONSULTATION',
    CONTROLE = 'CONTROLE',
    URGENCE = 'URGENCE',
    AUTRE = 'AUTRE'
}

export enum StatutRendezVous {
    PLANIFIE = 'PLANIFIE',
    CONFIRME = 'CONFIRME',
    PRESENT = 'PRESENT',
    EN_CONSULTATION = 'EN_CONSULTATION',
    TERMINE = 'TERMINE',
    ANNULE = 'ANNULE'
}

export interface RendezVous {
    id?: number;
    idPatient: number;
    idMedecin: number;
    dateRdv: string; // ISO date YYYY-MM-DD
    heureRdv: string; // HH:mm:ss
    motif: MotifRendezVous;
    statut?: StatutRendezVous;
    ordrePassage?: number;
    heureArrivee?: string;
    dateCreation?: string;
    dateModification?: string;

    // Virtual fields for UI
    patientName?: string;
}

export interface CreateRendezVousDTO {
    idPatient: number;
    idMedecin: number;
    dateRdv: string;
    heureRdv: string;
    motif: MotifRendezVous;
}

export interface UpdateRendezVousDTO {
    dateRdv: string;
    heureRdv: string;
    motif: MotifRendezVous;
}
