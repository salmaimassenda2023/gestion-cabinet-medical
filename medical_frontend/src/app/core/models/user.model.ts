export interface User {
    id: string;
    name: string;
    email: string;
    phone: string;
    status: 'active' | 'deactivate';
    role: string;
    signature?: string;
}
