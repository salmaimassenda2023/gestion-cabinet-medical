import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Clinic } from '../clinic-table/clinic-table.component';

@Component({
    selector: 'app-clinic-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './clinic-form.component.html',
    styleUrls: ['./clinic-form.component.css']
})
export class ClinicFormComponent implements OnInit, OnChanges {
    @Input() clinic?: Clinic;
    @Input() doctors: { id: string, name: string }[] = []; // List of doctors for selection
    @Output() save = new EventEmitter<Partial<Clinic>>();
    @Output() cancel = new EventEmitter<void>();

    clinicForm!: FormGroup;

    constructor(private fb: FormBuilder) { }

    ngOnInit(): void {
        this.initForm();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['clinic'] && this.clinicForm) {
            this.initForm();
        }
    }

    private initForm(): void {
        this.clinicForm = this.fb.group({
            name: [this.clinic?.name || '', [Validators.required, Validators.minLength(3)]],
            address: [this.clinic?.address || '', [Validators.required]],
            phone: [this.clinic?.phone || '', [Validators.required]],
            specialty: [this.clinic?.specialty || '', [Validators.required]],
            doctor: [this.clinic?.doctor || '', [Validators.required]],
            status: [this.clinic?.status || 'active', [Validators.required]],
            logo: [this.clinic?.logo || '']
        });
    }

    onFileSelected(event: any): void {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = (e: any) => {
                this.clinicForm.patchValue({ logo: e.target.result });
            };
            reader.readAsDataURL(file);
        }
    }

    onSubmit(): void {
        if (this.clinicForm.valid) {
            this.save.emit(this.clinicForm.value);
        }
    }

    onCancel(): void {
        this.cancel.emit();
    }
}
