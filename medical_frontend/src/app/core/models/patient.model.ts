export interface Patient {
    id?: number;
    cin: string;
    nom: string;
    prenom: string;
    dateNaissance: string; // ISO format
    sexe: 'M' | 'F';
    telephone?: string;
    email?: string;
    adresse?: string;
    typeMutuelle?: string;
    numeroMutuelle?: string;
    idCabinet?: number;
    actif?: boolean;
    createdAt?: string;
}
