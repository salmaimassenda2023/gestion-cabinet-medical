import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from '../../shared/components/header/header.component';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';

@Component({
  selector: 'app-secretary-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, HeaderComponent, SidebarComponent],
  template: `
    <div class="container">
        <app-header role="secretary" (logout)="onLogout()" (searchChange)="onSearch($event)"></app-header>
        <app-sidebar mode="secretary"></app-sidebar>
        <div class="main-content">
            <router-outlet></router-outlet>
        </div>
    </div>
    `,
  styles: [`
        .container {
            display: flex;
            min-height: 100vh;
            width: 100vw;
            position: relative;
            background: #f5f7fa;
            margin: 0 !important;
            padding-left: 0 !important;
            max-width: none;
        }
        .main-content {
            flex: 1;
            margin-left: 100px;
            padding: 100px 20px 20px 20px;
            display: flex;
            flex-direction: column;
            width: 100% !important;
            margin-right: 0 !important;
            padding-right: 0 !important;
        }
    `]
})
export class SecretaryDashboardComponent {
  onLogout() {
    console.log('Logging out...');
  }

  onSearch(term: string) {
    console.log('Searching for:', term);
  }
}
