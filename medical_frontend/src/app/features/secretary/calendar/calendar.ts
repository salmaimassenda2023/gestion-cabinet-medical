import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { AppointmentFormComponent } from './appointment-form/appointment-form.component';

interface Appointment {
    id: number;
    patientName: string;
    time: string;
    date?: string;
    type: string;
    status: 'scheduled' | 'pending' | 'completed';
    note?: string;
}

@Component({
    selector: 'app-secretary-calendar',
    standalone: true,
    imports: [CommonModule, ModalComponent, AppointmentFormComponent],
    templateUrl: './calendar.html',
    styleUrls: ['./calendar.css']
})
export class SecretaryCalendarComponent implements OnInit {
    currentDate = new Date();
    days: number[] = [];
    weekDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    isModalOpen = false;
    isDeleteModalOpen = false;
    selectedAppointment?: Appointment;

    activeTypeFilter: string = 'all';
    activeStatusFilter: string = 'all';

    appointments: Appointment[] = Array.from({ length: 30 }, (_, i) => ({
        id: i + 1,
        patientName: [
            'Peter Mullin', 'Sandra Bay', 'Andrew Kim', 'Alfred Murray', 'Sandra Bay',
            'Tom Young', 'Bob Fisher', 'Andrew Kim', 'Peter Mullin', 'Alfred Murray'
        ][i % 10] + (i > 9 ? ` ${Math.floor(i / 10) + 1}` : ''),
        time: `${Math.floor(8 + (i * 15) / 60).toString().padStart(2, '0')}:${((i * 15) % 60).toString().padStart(2, '0')}`,
        type: ['Consultation', 'Follow-up', 'Surgery', 'Checkup'][i % 4],
        status: ['scheduled', 'pending', 'completed'][i % 3] as any,
        note: i % 3 === 0 ? 'Regular checkup needed.' : ''
    }));

    ngOnInit() {
        this.generateCalendar();
    }

    get filteredAppointments(): Appointment[] {
        return this.appointments.filter(appt => {
            const typeMatch = this.activeTypeFilter === 'all' || appt.type === this.activeTypeFilter;
            const statusMatch = this.activeStatusFilter === 'all' || appt.status === this.activeStatusFilter;
            return typeMatch && statusMatch;
        });
    }

    generateCalendar() {
        const year = this.currentDate.getFullYear();
        const month = this.currentDate.getMonth();
        const daysInMonth = new Date(year, month + 1, 0).getDate();
        this.days = Array.from({ length: daysInMonth }, (_, i) => i + 1);
    }

    get monthName(): string {
        return this.currentDate.toLocaleString('default', { month: 'long' });
    }

    nextMonth() {
        this.currentDate = new Date(this.currentDate.setMonth(this.currentDate.getMonth() + 1));
        this.generateCalendar();
    }

    previousMonth() {
        this.currentDate = new Date(this.currentDate.setMonth(this.currentDate.getMonth() - 1));
        this.generateCalendar();
    }

    openAddModal() {
        this.selectedAppointment = undefined;
        this.isModalOpen = true;
    }

    editAppointment(appt: Appointment) {
        this.selectedAppointment = appt;
        this.isModalOpen = true;
    }

    closeAddModal() {
        this.isModalOpen = false;
        this.selectedAppointment = undefined;
    }

    onSaveAppointment(data: any) {
        if (this.selectedAppointment) {
            const index = this.appointments.findIndex(a => a.id === this.selectedAppointment?.id);
            if (index > -1) {
                this.appointments[index] = {
                    ...this.selectedAppointment,
                    patientName: data.patientName,
                    time: data.hour,
                    date: data.date,
                    type: data.type,
                    note: data.note
                };
                this.appointments = [...this.appointments];
            }
        } else {
            const newAppt: Appointment = {
                id: this.appointments.length > 0 ? Math.max(...this.appointments.map(a => a.id)) + 1 : 1,
                patientName: data.patientName,
                time: data.hour,
                date: data.date,
                type: data.type,
                status: 'scheduled',
                note: data.note
            };
            this.appointments = [newAppt, ...this.appointments];
        }
        this.closeAddModal();
    }

    onDeleteAppointment() {
        this.isModalOpen = false;
        this.isDeleteModalOpen = true;
    }

    confirmDelete() {
        if (this.selectedAppointment) {
            this.appointments = this.appointments.filter(a => a.id !== this.selectedAppointment?.id);
            this.isDeleteModalOpen = false;
            this.selectedAppointment = undefined;
        }
    }

    setTypeFilter(type: string) {
        this.activeTypeFilter = type;
    }

    setStatusFilter(status: string) {
        this.activeStatusFilter = status;
    }
}
