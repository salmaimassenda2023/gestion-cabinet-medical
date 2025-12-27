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
    this.router.navigate(['/doctor-onboarding'], { queryParams: { plan } }); // Updated path to match likely route
  }

  plans = [
    {
      name: "Monthly",
      price: "$49",
      period: "/month",
      description: "Perfect for getting started",
      features: [
        "Up to 200 appointments/month",
        "Patient management",
        "Digital records storage",
        "Payment processing",
        "Email support",
      ],
      popular: false,
      cta: "Start",
    },
    {
      name: "Annual",
      price: "$39",
      period: "/month",
      description: "Save 20% with yearly billing",
      originalPrice: "$49",
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
