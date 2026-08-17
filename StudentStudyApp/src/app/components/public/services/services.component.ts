import { Component } from '@angular/core';

@Component({
  selector: 'app-services',
  standalone: false,
  templateUrl: './services.component.html',
  styleUrl: './services.component.css'
})
export class ServicesComponent {
  services = [
    { theme: 'classroom', title: 'Classroom Training', tag: 'In-person labs', image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/java/java-original.svg', text: 'Structured instruction, hands-on labs, code reviews, and guided project practice so learners build a strong technical foundation.' },
    { theme: 'online', title: 'Online Training', tag: 'Live mentor sessions', image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/angular/angular-original.svg', text: 'Live interaction, mentor support, practical assignments, and flexible schedules for students and working professionals.' },
    { theme: 'corporate', title: 'Corporate Training', tag: 'Team upskilling', image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/docker/docker-original.svg', text: 'Team-focused programs for programming, AI, analytics, databases, full stack development, and enterprise tools.' },
    { theme: 'academic', title: 'Academic Project Development', tag: 'IEEE and final year', image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/tensorflow/tensorflow-original.svg', text: 'Planning, building, documenting, and presenting academic, IEEE, mini, major, and final-year projects using current technologies.' },
    { theme: 'internship', title: 'Internship', tag: 'Industry exposure', image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/git/git-original.svg', text: 'Structured exposure to real workflows, task planning, implementation, reporting, and professional communication.' },
    { theme: 'realtime', title: 'Real-Time Projects', tag: 'Client software', image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/nodejs/nodejs-original.svg', text: 'Custom web, mobile, AI, automation, and database-driven solutions translated from requirements into maintainable software.' }
  ];
}
