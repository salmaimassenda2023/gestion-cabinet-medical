import { Component, HostListener, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule, Menu, X } from 'lucide-angular';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, LucideAngularModule, RouterModule],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css']
})
export class NavbarComponent {
  isScrolled = signal(false);
  isMobileMenuOpen = signal(false);

  readonly MenuIcon = Menu;
  readonly XIcon = X;

  navItems = [
    { label: "Home", href: "#home" },
    { label: "For Doctors", href: "#doctors" },
    { label: "For Patients", href: "#patients" },
    { label: "Choice", href: "#usertype" },
  ];

  @HostListener('window:scroll', [])
  onWindowScroll() {
    this.isScrolled.set(window.scrollY > 50);
  }

  toggleMobileMenu() {
    this.isMobileMenuOpen.update(v => !v);
  }

  closeMobileMenu() {
    this.isMobileMenuOpen.set(false);
  }
  scrollToSection(sectionId: string) {
    const element = document.getElementById(sectionId);
    if (element) {
        element.scrollIntoView({ 
            behavior: 'smooth',
            block: 'start'
        });
    }
}
}
