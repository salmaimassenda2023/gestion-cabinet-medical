import { Component, Input, Output, EventEmitter, OnInit, ViewChild, ElementRef, AfterViewInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { User } from '../../../core/models/user.model';


@Component({
    selector: 'app-user-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './user-form.component.html',
    styleUrls: ['./user-form.component.css']
})
export class UserFormComponent implements OnInit, AfterViewInit, OnChanges {
    @Input() user?: User;
    @Input() isDoctor: boolean = false;
    @Output() save = new EventEmitter<Partial<User>>();
    @Output() cancel = new EventEmitter<void>();

    @ViewChild('signatureCanvas') canvas?: ElementRef<HTMLCanvasElement>;

    userForm!: FormGroup;
    captureMode: 'upload' | 'draw' = 'draw';
    private ctx?: CanvasRenderingContext2D | null;
    private isDrawing = false;

    constructor(private fb: FormBuilder) { }

    ngOnInit(): void {
        this.initForm();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['user'] && this.userForm) {
            this.initForm();
            if (this.captureMode === 'draw') {
                setTimeout(() => this.clearCanvas(), 0);
            }
        }
    }

    private initForm(): void {
        this.userForm = this.fb.group({
            name: [this.user?.name || '', [Validators.required, Validators.minLength(3)]],
            email: [this.user?.email || '', [Validators.required, Validators.email]],
            phone: [this.user?.phone || '', [Validators.required]],
            status: [this.user?.status || 'active', [Validators.required]],
            signature: [this.user?.signature || '']
        });
    }

    ngAfterViewInit(): void {
        if (this.captureMode === 'draw') {
            this.initCanvas();
        }
    }

    initCanvas(): void {
        if (this.canvas) {
            this.ctx = this.canvas.nativeElement.getContext('2d');
            if (this.ctx) {
                this.ctx.strokeStyle = '#000';
                this.ctx.lineWidth = 2;
                this.ctx.lineCap = 'round';
            }
        }
    }

    setCaptureMode(mode: 'upload' | 'draw'): void {
        this.captureMode = mode;
        if (mode === 'draw') {
            setTimeout(() => this.initCanvas(), 0);
        }
    }

    startDrawing(event: MouseEvent | TouchEvent): void {
        this.isDrawing = true;
        const pos = this.getPos(event);
        this.ctx?.beginPath();
        this.ctx?.moveTo(pos.x, pos.y);
    }

    draw(event: MouseEvent | TouchEvent): void {
        if (!this.isDrawing || !this.ctx) return;
        const pos = this.getPos(event);
        this.ctx.lineTo(pos.x, pos.y);
        this.ctx.stroke();
        event.preventDefault();
    }

    stopDrawing(): void {
        this.isDrawing = false;
        this.updateSignatureFromCanvas();
    }

    clearCanvas(): void {
        if (this.ctx && this.canvas) {
            this.ctx.clearRect(0, 0, this.canvas.nativeElement.width, this.canvas.nativeElement.height);
            this.userForm.patchValue({ signature: '' });
        }
    }

    private getPos(event: MouseEvent | TouchEvent): { x: number, y: number } {
        const rect = this.canvas!.nativeElement.getBoundingClientRect();
        const clientX = 'touches' in event ? event.touches[0].clientX : event.clientX;
        const clientY = 'touches' in event ? event.touches[0].clientY : event.clientY;
        return {
            x: clientX - rect.left,
            y: clientY - rect.top
        };
    }

    private updateSignatureFromCanvas(): void {
        if (this.canvas) {
            const dataUrl = this.canvas.nativeElement.toDataURL();
            this.userForm.patchValue({ signature: dataUrl });
        }
    }

    onFileSelected(event: any): void {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = (e: any) => {
                this.userForm.patchValue({ signature: e.target.result });
            };
            reader.readAsDataURL(file);
        }
    }

    onSubmit(): void {
        if (this.userForm.valid) {
            this.save.emit(this.userForm.value);
        }
    }

    onCancel(): void {
        this.cancel.emit();
    }
}
