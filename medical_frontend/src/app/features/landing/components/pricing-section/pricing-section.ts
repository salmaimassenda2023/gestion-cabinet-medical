import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-pricing-section',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pricing-section.html',
  styleUrls: ['./pricing-section.css']
})
export class PricingSectionComponent {
  constructor(private router: Router) { }

  navigateToOnboarding(plan: string) {
    this.router.navigate(['/onboarding'], { queryParams: { plan } }); // Updated path to match likely route
  }

  plans = [
    
    {
      name: "Monthly",
      price: "350 DH",
      period: "/month",
      description: "Perfect for small practices",
      features: [
        "Up to 50 appointments/month",
        "Basic patient management",
        "Standard support",
        "Manual payment handling"
      ],
      originalPrice: "600 DH",
      popular: false,
      cta: "Start",
    },
    {
      name: "Manual",
      price: "3000 DH",
      period: "/an",
      description: "Full access for growing clinics",
      features: [
        "Unlimited appointments",
        "Advanced patient management",
        "Unlimited storage",
        "Priority payment processing",
        "Analytics & reporting",
        "Priority support",
        "Custom branding",
      ],
      popular: true,
      cta: "Start",
    },
  ];
}
