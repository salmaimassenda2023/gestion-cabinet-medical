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
    const planDetails = this.plans.find(p => p.name.toLowerCase() === plan.toLowerCase());
    if (planDetails) {
      localStorage.setItem('selected_plan', JSON.stringify({
        name: plan,
        price: this.getPlanPrice(plan),
        period: planDetails.period,
        typePeriode: plan.toLowerCase() === 'monthly' ? 'MENSUEL' : 'ANNUEL'
      }));
    }
    this.router.navigate(['/onboarding'], { queryParams: { plan: plan.toLowerCase() } });
  }

  getPlanPrice(planName: string): number {
    const plan = this.plans.find(p => p.name.toLowerCase() === planName.toLowerCase());
    if (!plan) return 0;
    
    const priceMatch = plan.price.match(/\d+/);
    return priceMatch ? parseFloat(priceMatch[0]) : 0;
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
      name: "Annual",
      price: "3000 DH",
      period: "/year",
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