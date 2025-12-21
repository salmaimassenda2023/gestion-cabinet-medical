import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule, Facebook, Twitter, Linkedin, Instagram, Mail, Phone, MapPin } from 'lucide-angular';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './footer.html',
  styleUrls: ['./footer.css']
})
export class FooterComponent {
  readonly FacebookIcon = Facebook;
  readonly TwitterIcon = Twitter;
  readonly LinkedinIcon = Linkedin;
  readonly InstagramIcon = Instagram;
  readonly MailIcon = Mail;
  readonly PhoneIcon = Phone;
  readonly MapPinIcon = MapPin;

  footerLinks = [
    { category: "Product", links: ["Features", "Pricing", "Security", "Integrations"] },
    { category: "Company", links: ["About Us", "Careers", "Blog", "Press"] },
    { category: "Resources", links: ["Help Center", "Documentation", "API Reference", "Status"] },
    { category: "Legal", links: ["Privacy Policy", "Terms of Service", "Cookie Policy", "HIPAA Compliance"] },
  ];

  socialLinks = [
    { icon: Facebook, href: "#" },
    { icon: Twitter, href: "#" },
    { icon: Linkedin, href: "#" },
    { icon: Instagram, href: "#" },
  ];

  currentYear = new Date().getFullYear();
}
