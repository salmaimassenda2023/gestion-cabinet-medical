import { Component, signal, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule, User, Stethoscope, Bot, Send, Calendar, DollarSign, MapPin, Clock } from 'lucide-angular';
import { PricingSectionComponent } from '../pricing-section/pricing-section';
import { ChatbotService } from '../../../../core/services/chatbot.service';
import { CabinetService } from '../../../doctor/services/cabinet.service';

interface ChatMessage {
  role: 'user' | 'bot' | 'system';
  message: string;
  data?: any;
}

@Component({
  selector: 'app-user-type-section',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule, PricingSectionComponent],
  templateUrl: './user-type-section.html',
  styleUrls: ['./user-type-section.css']
})
export class UserTypeSectionComponent implements OnInit {
  userType = signal<'doctor' | 'patient' | null>(null);
  chatMessage = signal('');
  chatHistory = signal<ChatMessage[]>([
    {
      role: "bot",
      message: "Hello! I'm your Clinic Flow assistant. I can help you: \n1️⃣ Find clinics by specialty/location \n2️⃣ Check doctor availability \n3️⃣ View service prices \n\nHow can I assist you today?"
    }
  ]);
  suggestions = signal<string[]>([
    "Find cardiology clinics",
    "Check availability tomorrow",
    "View prices",
    "General practitioners near me"
  ]);
  specialites = signal<string[]>([]);
  isLoading = signal(false);

  readonly UserIcon = User;
  readonly StethoscopeIcon = Stethoscope;
  readonly BotIcon = Bot;
  readonly SendIcon = Send;
  readonly CalendarIcon = Calendar;
  readonly DollarSignIcon = DollarSign;
  readonly MapPinIcon = MapPin;
  readonly ClockIcon = Clock;

  private chatbotService = inject(ChatbotService);
  private cabinetService = inject(CabinetService);

  constructor() { }

  ngOnInit(): void {
    this.loadSpecialites();
  }

  setUserType(type: 'doctor' | 'patient') {
    this.userType.set(type);
  }

  loadSpecialites() {
    this.chatbotService.getSpecialites().subscribe({
      next: (specialites) => {
        this.specialites.set(specialites);
      },
      error: (error) => {
        console.error('Error loading specialties:', error);
      }
    });
  }

  handleSendMessage() {
    const message = this.chatMessage().trim();
    if (!message) return;

    this.chatHistory.update(prev => [...prev, { role: "user", message }]);
    this.chatMessage.set("");
    this.isLoading.set(true);

    this.processMessage(message);
  }

  async processMessage(message: string) {
    const lowerMsg = message.toLowerCase();

    try {
      if (lowerMsg.includes('find') || lowerMsg.includes('search') || lowerMsg.includes('clinic')) {
        await this.handleSearchRequest(message);
      }
      else if (lowerMsg.includes('available') || lowerMsg.includes('time') || lowerMsg.includes('slot')) {
        await this.handleAvailabilityRequest(message);
      }
      else if (lowerMsg.includes('price') || lowerMsg.includes('cost') || lowerMsg.includes('tarif')) {
        await this.handlePriceRequest(message);
      }
      else {
        const response = await this.chatbotService.getBotResponse(message).toPromise();
        this.addBotMessage(response || "I can help you find clinics, check availability, and view prices. What specifically would you like to know?");
      }
    } catch (error) {
      this.addBotMessage("Sorry, I encountered an error. Please try again or be more specific.");
      console.error('Chatbot error:', error);
    } finally {
      this.isLoading.set(false);
    }
  }

  async handleSearchRequest(message: string) {
    const params: any = {};

    this.specialites().forEach(specialite => {
      if (message.toLowerCase().includes(specialite.toLowerCase())) {
        params.specialite = specialite;
      }
    });

    if (message.toLowerCase().includes('near me') || message.toLowerCase().includes('nearby')) {
      params.adresse = 'current location';
    }

    this.chatbotService.searchCabinets(params).subscribe({
      next: (cabinets) => {
        if (cabinets.length === 0) {
          this.addBotMessage(`No clinics found matching your criteria. Try a different specialty or location.`);
          return;
        }

        this.addBotMessage(`I found ${cabinets.length} clinic(s):`, cabinets);

        this.suggestions.set([
          "Check availability for " + cabinets[0].nom,
          "View all prices",
          "Find more clinics"
        ]);
      },
      error: (error) => {
        this.addBotMessage("Sorry, I couldn't search for clinics right now. Please try again later.");
      }
    });
  }

  async handleAvailabilityRequest(message: string) {
    let date = 'tomorrow';
    if (message.includes('today')) date = 'today';
    if (message.includes('next week')) date = 'next week';
    this.addBotMessage(`To check availability, please specify: \n1) The clinic name \n2) A specific date \n\nExample: "Is Dr. Smith available on Friday?"`);
  }

  checkAvailability(cabinetId: number) {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const dateStr = tomorrow.toISOString().split('T')[0];

    this.isLoading.set(true);
    this.chatbotService.checkDisponibilite(cabinetId, dateStr).subscribe({
      next: (resp) => {
        if (resp.creneauxDisponibles.length === 0) {
          this.addBotMessage(`No availability found for ${resp.cabinetNom} on ${resp.date}.`);
        } else {
          const slots = resp.creneauxDisponibles.join(', ');
          this.addBotMessage(`Available slots for ${resp.cabinetNom} on ${resp.date}: \n${slots}`);
        }
        this.isLoading.set(false);
      },
      error: (error) => {
        this.addBotMessage("Sorry, I couldn't check availability right now.");
        this.isLoading.set(false);
      }
    });
  }

  async handlePriceRequest(message: string) {
    // Get all cabinets to show price ranges
    this.cabinetService.getAllCabinets().subscribe({
      next: (cabinets) => {
        if (cabinets.length === 0) {
          this.addBotMessage("No clinics available at the moment.");
          return;
        }

        const priceRange = {
          min: Math.min(...cabinets.map(c => c.tarifConsultation)),
          max: Math.max(...cabinets.map(c => c.tarifConsultation)),
          average: cabinets.reduce((sum, c) => sum + c.tarifConsultation, 0) / cabinets.length
        };

        this.addBotMessage(
          `Consultation prices range from ${priceRange.min} to ${priceRange.max} TND, with an average of ${priceRange.average.toFixed(2)} TND. \n\n` +
          `Would you like to see specific clinics in a price range?`
        );
      },
      error: (error) => {
        this.addBotMessage("Unable to fetch price information at the moment.");
      }
    });
  }

  addBotMessage(message: string, data?: any) {
    this.chatHistory.update(prev => [...prev, {
      role: "bot",
      message,
      data
    }]);
  }

  handleSuggestion(suggestion: string) {
    this.chatMessage.set(suggestion);
    this.handleSendMessage();
  }

  // Helper to format cabinet data for display
  formatCabinetData(cabinets: any[]): string {
    return cabinets.map(cabinet =>
      `🏥 ${cabinet.nom}\n` +
      `📍 ${cabinet.adresse}\n` +
      `👨‍⚕️ ${cabinet.specialite}\n` +
      `💰 ${cabinet.tarifConsultation} TND\n` +
      `📞 ${cabinet.numTel}\n`
    ).join('\n---\n');
  }
}