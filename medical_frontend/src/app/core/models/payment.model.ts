export interface Service {
    id: string;
    name: string;
    price: number;
}

export interface PaymentRecord {
    id: string;
    patientName: string;
    services: Service[];
    consultationPrice: number;
    totalPrice: number;
    date: string;
    status: 'paid' | 'pending';
}
