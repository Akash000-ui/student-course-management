import { Component } from '@angular/core';

@Component({
  selector: 'app-projects',
  standalone: false,
  templateUrl: './projects.component.html',
  styleUrl: './projects.component.css'
})
export class ProjectsComponent {
  steps = [
    { title: 'Topic selection', text: 'Pick a domain and problem statement aligned to your branch and academic requirement.' },
    { title: 'Requirements', text: 'Convert the idea into modules, scope, features, and implementation milestones.' },
    { title: 'Design', text: 'Prepare flow, database, UI, diagrams, and technology plan before coding starts.' },
    { title: 'Implementation', text: 'Build the application with mentor checkpoints, testing guidance, and debugging support.' },
    { title: 'Documentation', text: 'Create reports, abstracts, PPT support, and final review material.' }
  ];

  domains = [
    { title: 'Web Apps', icon: 'language', image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/react/react-original.svg', text: 'Portals, dashboards, student systems, ecommerce, admin panels, and custom web products.' },
    { title: 'Android', icon: 'android', image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/android/android-original.svg', text: 'Mobile applications with database, authentication, APIs, and project documentation.' },
    { title: 'AI / ML', icon: 'psychology', image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/tensorflow/tensorflow-original.svg', text: 'Prediction, recommendation, classification, image models, NLP, and AI prototypes.' },
    { title: 'Cloud', icon: 'cloud_queue', image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/amazonwebservices/amazonwebservices-original-wordmark.svg', text: 'Cloud-ready apps, deployment guidance, hosting flow, and scalable project architecture.' },
    { title: 'Big Data', icon: 'dataset', image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/mongodb/mongodb-original.svg', text: 'Data processing, mining, analytics dashboards, storage, and reporting workflows.' },
    { title: 'Image Processing', icon: 'image_search', image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/opencv/opencv-original.svg', text: 'Computer vision, detection, recognition, segmentation, and visual data experiments.' },
    { title: 'Networking', icon: 'hub', image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/docker/docker-original.svg', text: 'Network simulation, security workflows, monitoring concepts, and system design.' },
    { title: 'Information Security', icon: 'security', image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/linux/linux-original.svg', text: 'Security-aware projects, authentication, access control, encryption concepts, and audits.' }
  ];
}
