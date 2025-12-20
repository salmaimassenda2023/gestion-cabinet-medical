import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule, User, Stethoscope, Bot, Send } from 'lucide-angular';
import { PricingSectionComponent } from '../pricing-section/pricing-section';

interface ChatMessage {
  role: 'user' | 'bot';
  message: string;
}

@Component({
  selector: 'app-user-type-section',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule, PricingSectionComponent],
  templateUrl: './user-type-section.html',
  styleUrls: ['./user-type-section.css']
})
export class UserTypeSectionComponent {
  userType = signal<'doctor' | 'patient' | null>(null);
  chatMessage = signal('');
  chatHistory = signal<ChatMessage[]>([
    { role: "bot", message: "Hello! I'm your Clinic Flow assistant. How can I help you today? You can ask me about finding clinics, booking appointments, or pricing information." }
  ]);

  readonly UserIcon = User;
  readonly StethoscopeIcon = Stethoscope;
  readonly BotIcon = Bot;
  readonly SendIcon = Send;

  suggestions = ["Find available clinics", "How to book?", "View pricing"];

  setUserType(type: 'doctor' | 'patient') {
    this.userType.set(type);
  }

  handleSendMessage() {
    const message = this.chatMessage().trim();
    if (!message) return;

    this.chatHistory.update(prev => [...prev, { role: "user", message }]);
    const currentMessage = message; // Capture for closure
    this.chatMessage.set("");

    // Simulated bot response
    setTimeout(() => {
      this.chatHistory.update(prev => [...prev, {
        role: "bot",
        message: "Thank you for your question! Our team is working on connecting this chatbot to provide real-time assistance. In the meantime, you can explore our clinic listings or contact us directly for help."
      }]);
    }, 1000);
  }

  handleSuggestion(suggestion: string) {
    this.chatMessage.set(suggestion);
  }
}
