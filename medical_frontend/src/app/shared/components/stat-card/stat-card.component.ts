import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  template: `
    <div class="backdrop-blur-[40px] bg-white/25 border border-white/40 rounded-2xl p-6 flex flex-col gap-3 min-w-[220px] flex-1 transition-all duration-300 hover:scale-[1.02] hover:bg-white/35 shadow-lg shadow-cyan-900/10">
      <span class="text-slate-700/90 text-sm font-medium tracking-wide uppercase">{{ title }}</span>
      <div class="flex items-center gap-4">
        <div class="stat-icon p-3 rounded-xl bg-white/50 backdrop-blur-sm border border-white/30 shadow-sm flex items-center justify-center">
          <lucide-icon [img]="icon" class="w-6 h-6 text-slate-700"></lucide-icon>
        </div>
        <div>
          <span class="text-2xl font-bold text-slate-800">{{ value }}</span>
          <span class="text-slate-600 text-sm ml-2">{{ subtitle }}</span>
        </div>
      </div>
    </div>
  `
})
export class StatCardComponent {
  @Input() title: string = '';
  @Input() value: string = '';
  @Input() subtitle: string = '';
  @Input() icon: any; // Lucide icon
}
