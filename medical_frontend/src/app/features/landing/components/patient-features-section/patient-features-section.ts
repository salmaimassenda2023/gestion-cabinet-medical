import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule, Search, Calendar, DollarSign, MessageCircle, Clock } from 'lucide-angular';

@Component({
  selector: 'app-patient-features-section',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './patient-features-section.html',
  styleUrls: ['./patient-features-section.css']
})
export class PatientFeaturesSectionComponent {
  features = [
    {
      icon: Search,
      title: "Find Available Clinics",
      description: "Browse and discover healthcare providers in your area with real-time availability.",
    },
    {
      icon: Calendar,
      title: "View Appointments",
      description: "See open appointment slots instantly and book at your convenience.",
    },
    {
      icon: DollarSign,
      title: "Transparent Pricing",
      description: "Know exactly what you'll pay before booking. No hidden fees or surprises.",
    },
    {
      icon: MessageCircle,
      title: "Instant Chat Support",
      description: "Get answers to your questions 24/7 with our AI-powered assistant.",
    },
    {
      icon: Clock,
      title: "Easy Booking Process",
      description: "Book appointments in seconds with our streamlined scheduling system.",
    },
  ];
}
