import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SharedButtonComponent } from '../../../shared/components/button/button.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, SharedButtonComponent],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginComponent {
  username = signal('');
  password = signal('');
  isLoading = signal(false);

  constructor(private router: Router) { }

  onSubmit() {
    this.isLoading.set(true);

    console.log('Login attempt:', {
      username: this.username(),
      password: this.password()
    });

    // Simulate API call
    setTimeout(() => {
      this.isLoading.set(false);
      // this.router.navigate(['/dashboard']);
    }, 1500);
  }
}