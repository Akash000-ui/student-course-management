import { Component } from '@angular/core';

@Component({
  selector: 'app-internship',
  standalone: false,
  templateUrl: './internship.component.html',
  styleUrl: './internship.component.css'
})
export class InternshipComponent {
  process = [
    { title: 'Register', text: 'Share your details, preferred domain, duration, and learning goal.' },
    { title: 'Select a domain', text: 'Choose Java, Python, AI, full stack, data science, Android, or web development.' },
    { title: 'Get offer letter', text: 'Receive confirmation and internship details after enrollment discussion.' },
    { title: 'Connect with team', text: 'Join mentor-led sessions, task reviews, and project checkpoints.' },
    { title: 'Gain experience', text: 'Work through practical assignments, implementation tasks, and reporting.' },
    { title: 'Get experience letter', text: 'Complete requirements and receive completion or experience documentation.' }
  ];

  benefits = [
    { title: 'Real workflow exposure', icon: 'account_tree', image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/git/git-original.svg' },
    { title: 'Project portfolio', icon: 'developer_board', image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/react/react-original.svg' },
    { title: 'Mentor checkpoints', icon: 'support_agent', image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/python/python-original.svg' },
    { title: 'Documentation support', icon: 'description', image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/java/java-original.svg' }
  ];
}
