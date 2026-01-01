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
      name: "Manual",
      price: "Free",
      period: "/forever",
      description: "Perfect for testing or small practices",
      features: [
        "Up to 50 appointments/month",
        "Basic patient management",
        "Standard support",
        "Manual payment handling"
      ],
      popular: false,
      cta: "Start",
    },
    {
      name: "Monthly",
      price: "$29.99",
      period: "/month",
      description: "Full access for growing clinics",
      originalPrice: "$49.99",
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
