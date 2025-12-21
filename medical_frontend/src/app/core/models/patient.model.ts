export interface Patient {
    id: string;
    cin: string;
    name: string;
    age: number;
    mutuelleType: string;
    gender: 'Male' | 'Female';
}
