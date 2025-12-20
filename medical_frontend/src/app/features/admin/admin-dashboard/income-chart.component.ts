import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

interface ChartData {
  name: string;
  value: number;
}

@Component({
  selector: 'app-income-chart',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="chart-wrapper">
      <svg class="chart-svg" viewBox="0 50 1000 250" preserveAspectRatio="xMidYMid meet">
        <!-- Y-axis grid lines -->
        <g class="grid-lines">
          <line x1="60" y1="50" x2="740" y2="50" stroke="rgba(0,0,0,0.1)" stroke-width="1" stroke-dasharray="2,2" />
          <line x1="60" y1="100" x2="740" y2="100" stroke="rgba(0,0,0,0.1)" stroke-width="1" stroke-dasharray="2,2" />
          <line x1="60" y1="150" x2="740" y2="150" stroke="rgba(0,0,0,0.1)" stroke-width="1" stroke-dasharray="2,2" />
          <line x1="60" y1="200" x2="740" y2="200" stroke="rgba(0,0,0,0.1)" stroke-width="1" stroke-dasharray="2,2" />
          <line x1="60" y1="250" x2="740" y2="250" stroke="rgba(0,0,0,0.1)" stroke-width="1" stroke-dasharray="2,2" />
        </g>

        <!-- Area gradient -->
        <defs>
          <linearGradient id="areaGradient" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="hsl(197, 58%, 68%)" stop-opacity="0.3" />
            <stop offset="100%" stop-color="hsl(197, 58%, 68%)" stop-opacity="0" />
          </linearGradient>
        </defs>

        <!-- Area path -->
        <path [attr.d]="areaPath" fill="url(#areaGradient)" />
        
        <!-- Line path -->
        <path [attr.d]="linePath" fill="none" stroke="hsl(197, 58%, 55%)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
        
        <!-- Data points -->
        <g class="data-points">
          <circle *ngFor="let point of points" 
                  [attr.cx]="point.x" 
                  [attr.cy]="point.y" 
                  r="4" 
                  fill="hsl(197, 58%, 55%)"
                  stroke="white" 
                  stroke-width="2" />
        </g>
        
        <!-- Y-axis labels -->
        <g class="y-axis">
          <text x="40" y="50" text-anchor="end" class="axis-label">100%</text>
          <text x="40" y="100" text-anchor="end" class="axis-label">75%</text>
          <text x="40" y="150" text-anchor="end" class="axis-label">50%</text>
          <text x="40" y="200" text-anchor="end" class="axis-label">25%</text>
          <text x="40" y="250" text-anchor="end" class="axis-label">0%</text>
        </g>
        
        <!-- X-axis labels -->
        <g class="x-axis">
          <text *ngFor="let point of xAxisPoints" 
                [attr.x]="point.x" 
                y="280" 
                text-anchor="middle"
                class="axis-label">
            {{point.label}}
          </text>
        </g>
      </svg>
    </div>
  `,
  styles: [`
    :host {
      display: block;
      width: 100%;
      height: 100%;
    }
    
    .chart-wrapper {
      width: 100%;
      height: 100%;
      padding: 10px;
    }
    
    .chart-svg {
      width: 100%;
      height: 100%;
      overflow: visible;
    }
    
    .axis-label {
      font-size: 12px;
      fill: hsl(215.4, 16.3%, 46.9%);
      font-family: 'Inter', sans-serif;
      font-weight: 500;
    }
    
    .data-points circle {
      transition: r 0.2s ease;
    }
    
    .data-points circle:hover {
      r: 6;
      cursor: pointer;
    }
  `]
})
export class IncomeChartComponent implements OnChanges {
  @Input() data: ChartData[] = [];

  points: { x: number, y: number }[] = [];
  linePath: string = '';
  areaPath: string = '';
  xAxisPoints: { x: number, label: string }[] = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['data']) {
      this.calculatePaths();
    }
  }

  calculatePaths() {
    if (!this.data || this.data.length === 0) return;

    const maxY = 100; // Fixed max value of 100% for the chart
    const chartHeight = 250; // SVG height for calculations
    const chartWidth = 680; // SVG width (800 - 60 - 60 padding)
    const startX = 60; // Start X position
    const startY = 50; // Start Y position
    
    const xStep = chartWidth / (this.data.length - 1);

    // Calculate points
    this.points = this.data.map((d, i) => ({
      x: startX + (i * xStep),
      y: startY + (chartHeight - (d.value / maxY * chartHeight))
    }));

    // Generate X-axis labels
    this.xAxisPoints = this.data.map((d, i) => ({
      x: startX + (i * xStep),
      label: d.name
    }));

    // Build line path
    if (this.points.length > 0) {
      let line = `M ${this.points[0].x},${this.points[0].y}`;
      let area = `M ${this.points[0].x},${this.points[0].y}`;
      
      for (let i = 1; i < this.points.length; i++) {
        const prev = this.points[i - 1];
        const curr = this.points[i];
        
        // Control points for smoothing
        const cp1x = prev.x + (curr.x - prev.x) * 0.4;
        const cp1y = prev.y;
        const cp2x = curr.x - (curr.x - prev.x) * 0.4;
        const cp2y = curr.y;
        
        line += ` C ${cp1x},${cp1y} ${cp2x},${cp2y} ${curr.x},${curr.y}`;
        area += ` C ${cp1x},${cp1y} ${cp2x},${cp2y} ${curr.x},${curr.y}`;
      }
      
      this.linePath = line;
      
      // Close the area path
      const lastPoint = this.points[this.points.length - 1];
      const bottomY = startY + chartHeight;
      area += ` L ${lastPoint.x},${bottomY} L ${this.points[0].x},${bottomY} Z`;
      this.areaPath = area;
    }
  }
}