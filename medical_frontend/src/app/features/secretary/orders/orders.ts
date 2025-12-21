import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

interface ScheduledOrder {
    id: string;
    orderNumber: number;
    patientName: string;
    time: string;
    status: 'waiting' | 'in-consultation' | 'completed';
}

@Component({
    selector: 'app-secretary-orders',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './orders.html',
    styleUrls: ['./orders.css']
})
export class SecretaryOrdersComponent implements OnInit {
    orders: ScheduledOrder[] = [
        { id: '1', orderNumber: 1, patientName: 'Peter Mullin', time: '08:00 AM', status: 'in-consultation' },
        { id: '2', orderNumber: 2, patientName: 'Sandra Bay', time: '08:15 AM', status: 'waiting' },
        { id: '3', orderNumber: 3, patientName: 'Andrew Kim', age: 28, mutuelleType: 'Private', gender: 'Male', status: 'waiting' } as any,
        { id: '4', orderNumber: 4, patientName: 'Alfred Murray', time: '08:45 AM', status: 'waiting' }
    ];

    draggedIndex: number | null = null;

    ngOnInit() { }

    get currentPatient() {
        return this.orders.find(o => o.status === 'in-consultation');
    }

    onNext() {
        const currentIndex = this.orders.findIndex(o => o.status === 'in-consultation');
        if (currentIndex !== -1) {
            this.orders[currentIndex].status = 'completed';
            const nextIndex = this.orders.findIndex(o => o.status === 'waiting');
            if (nextIndex !== -1) {
                this.orders[nextIndex].status = 'in-consultation';
            }
        } else {
            const firstWaiting = this.orders.findIndex(o => o.status === 'waiting');
            if (firstWaiting !== -1) {
                this.orders[firstWaiting].status = 'in-consultation';
            }
        }
    }

    // HTML5 Drag and Drop
    onDragStart(index: number) {
        this.draggedIndex = index;
    }

    onDragOver(event: DragEvent) {
        event.preventDefault();
    }

    onDrop(index: number) {
        if (this.draggedIndex === null || this.draggedIndex === index) return;

        const movedItem = this.orders.splice(this.draggedIndex, 1)[0];
        this.orders.splice(index, 0, movedItem);

        // Re-assign order numbers
        this.orders.forEach((o, i) => o.orderNumber = i + 1);

        this.draggedIndex = null;
    }
}
