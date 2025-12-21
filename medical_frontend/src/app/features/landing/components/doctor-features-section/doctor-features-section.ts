import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule, CalendarDays, FileText, CreditCard, Users, BarChart3, Zap, ArrowRight } from 'lucide-angular';

@Component({
  selector: 'app-doctor-features-section',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './doctor-features-section.html',
  styleUrls: ['./doctor-features-section.css']
})
export class DoctorFeaturesSectionComponent {
  readonly ArrowRightIcon = ArrowRight;

  features = [
    {
      icon: CalendarDays,
      title: "Appointment Management",
      description: "Digital calendar with automated reminders, no-show tracking, and intelligent scheduling.",
    },
    {
      icon: FileText,
      title: "Digital Medical Records",
      description: "Secure, organized patient records with templates and document storage.",
    },
    {
      icon: CreditCard,
      title: "Payment Processing",
      description: "Integrated invoicing, payment tracking, and revenue analytics.",
    },
    {
      icon: Users,
      title: "Secretary Managment",
      description: "A management space where secretaries can work efficiently and handle patient requests.",
    },
    {
      icon: BarChart3,
      title: "Analytics & Reporting",
      description: "Comprehensive insights into your practice's performance and growth.",
    },
    {
      icon: Zap,
      title: "Easy Setup",
      description: "Get started in minutes with our intuitive onboarding wizard.",
    },
  ];
}
