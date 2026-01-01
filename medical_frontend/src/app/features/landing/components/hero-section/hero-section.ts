import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-hero-section',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './hero-section.html',
  styleUrls: ['./hero-section.css']
})
export class HeroSectionComponent {

  constructor(private router: Router) { }

 
}
