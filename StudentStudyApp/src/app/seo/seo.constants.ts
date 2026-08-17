export const SEO_SITE = {
  name: 'VOIDMAIN ACADEMY',
  legalName: 'VOIDMAIN ACADEMY',
  url: 'https://www.voidmainacademy.com',
  logo: 'https://www.voidmainacademy.com/logo.jpeg',
  image: 'https://www.voidmainacademy.com/logo.jpeg',
  email: 'info@voidmainacademy.com',
  primaryPhone: '+91-7331139207',
  supportPhone: '+91-7989743392',
  address: {
    streetAddress: 'Mallika Crown Complex, Beside Union Bank, Near Metro Station, Pillar Number 1529-1530, Dilsukhnagar Main Road',
    locality: 'Hyderabad',
    region: 'Telangana',
    postalCode: '500060',
    country: 'IN'
  },
  geo: {
    latitude: 17.3703437,
    longitude: 78.5231559
  },
  businessHours: [
    {
      '@type': 'OpeningHoursSpecification',
      dayOfWeek: ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
      opens: '07:00',
      closes: '21:00'
    },
    {
      '@type': 'OpeningHoursSpecification',
      dayOfWeek: 'Sunday',
      opens: '07:00',
      closes: '13:00'
    }
  ],
  keywords: [
    'software courses training in Hyderabad',
    'Java full stack course Dilsukhnagar',
    'Python full stack training Hyderabad',
    'AI course Hyderabad',
    'data science training Hyderabad',
    'data analytics course Hyderabad',
    'IEEE projects Hyderabad',
    'academic project guidance Hyderabad',
    'software internship Hyderabad',
    'full stack developer course Hyderabad',
    'Voidmain Academy'
  ]
};

export const DEFAULT_SEO = {
  title: 'VOIDMAIN ACADEMY | Software Courses Training in Hyderabad',
  description: 'VOIDMAIN ACADEMY provides Java full stack, Python full stack, AI, data science, data analytics, web development, internships, and academic project guidance in Dilsukhnagar, Hyderabad.',
  keywords: SEO_SITE.keywords.join(', '),
  image: SEO_SITE.image,
  type: 'website'
};
