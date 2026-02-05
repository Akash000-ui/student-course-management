```markdown
# 🎓 Student Course Management System

A full-stack web application for managing online courses, built with Spring Boot and Angular. Features include course management, video streaming, user enrollment, progress tracking, and email notifications.

## ✨ Features

### 👥 User Management
- User registration and login with JWT authentication
- Email verification with OTP (One-Time Password)
- Google OAuth integration
- Role-based access control (Admin/User)

### 📚 Course Management
- Create, update, and delete courses
- Course categorization and filtering
- Public course catalog (no login required)
- Course difficulty levels and language support
- Thumbnail upload and management

### 📹 Video Management
- Upload course videos
- Video streaming and playback
- Sequential video access control
- Video progress tracking

### 📊 Progress Tracking
- Track video completion status
- Course progress percentage
- User enrollment history
- Continue learning from last position

### 📧 Email Notifications
- Automated email alerts when courses are created/updated
- Batch processing (100 users/batch)
- SendGrid integration for reliable delivery
- OTP email verification

### 🔍 Search & Filter
- Search courses by title
- Filter by category, difficulty, and language
- Public course catalog page

## 🛠️ Tech Stack

### Backend
- **Framework:** Spring Boot 3.5.4
- **Language:** Java 17
- **Database:** MongoDB Atlas
- **Authentication:** JWT + Spring Security
- **Email:** SendGrid API
- **Build Tool:** Maven
- **Deployment:** Docker + Render

### Frontend
- **Framework:** Angular 17+
- **UI Library:** Angular Material
- **Styling:** CSS3
- **HTTP Client:** HttpClient
- **State Management:** RxJS
- **Deployment:** Netlify

## 📋 Prerequisites

- Java 17+
- Node.js 18+
- MongoDB Atlas account
- SendGrid account (free tier)
- Docker (for deployment)

## 🚀 Getting Started

### Backend Setup

1. **Clone the repository**
```bash
git clone https://github.com/Akash000-ui/student-course-management.git
cd student-course-management/Student-Course-Management
```

2. **Configure application.properties**
```properties
spring.data.mongodb.uri=your_mongodb_uri
jwt.secret=your_jwt_secret
jwt.expiration=3600000

sendgrid.api.key=your_sendgrid_api_key
sendgrid.from.email=your_verified_email
sendgrid.from.name=StudieHub

google.clientId=your_google_oauth_client_id
```

3. **Build and run**
```bash
./mvnw clean install
./mvnw spring-boot:run
```

Backend will run on `http://localhost:8080`

### Frontend Setup

1. **Navigate to frontend directory**
```bash
cd StudentStudyApp
```

2. **Install dependencies**
```bash
npm install
```

3. **Run development server**
```bash
ng serve
```

Frontend will run on `http://localhost:4200`

## 🔧 Environment Variables

### Backend (Render/Production)
```
SPRING_DATA_MONGODB_URI=mongodb+srv://user:password@cluster.mongodb.net/dbname
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=3600000
SENDGRID_API_KEY=SG.xxxxxxxxxxxxxxxxxxxxx
SENDGRID_FROM_EMAIL=your@email.com
SENDGRID_FROM_NAME=StudieHub
GOOGLE_CLIENTID=your_google_client_id
EMAIL_NOTIFICATION_ENABLED=true
EMAIL_NOTIFICATION_BATCH_SIZE=100
EMAIL_NOTIFICATION_BATCH_DELAY_MS=5000
```

### Frontend
Update service base URLs in Angular services to point to your backend:
- Development: `http://localhost:8080`
- Production: `https://your-backend.onrender.com`

## 🐳 Docker Deployment

### Build Docker Image
```bash
cd Student-Course-Management
docker build -t studiehub-backend:latest .
```

### Run Docker Container
```bash
docker run -p 8080:8080 \
  -e SPRING_DATA_MONGODB_URI=your_mongodb_uri \
  -e JWT_SECRET=your_jwt_secret \
  -e SENDGRID_API_KEY=your_api_key \
  studiehub-backend:latest
```

### Push to Docker Hub
```bash
docker tag studiehub-backend:latest yourusername/studiehub-backend:latest
docker push yourusername/studiehub-backend:latest
```

## 📱 API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/google` - Google OAuth login
- `POST /api/auth/login-otp` - OTP-based login

### OTP
- `POST /api/otp/send` - Send OTP to email
- `POST /api/otp/verify` - Verify OTP
- `POST /api/otp/resend` - Resend OTP

### Courses (Public)
- `GET /api/courses` - Get all courses
- `GET /api/courses/{id}` - Get course by ID
- `GET /api/categories` - Get all categories

### Courses (Admin)
- `POST /api/courses` - Create course
- `PUT /api/courses/{id}` - Update course
- `DELETE /api/courses/{id}` - Delete course

### Enrollments (User)
- `POST /api/enrollments/{courseId}` - Enroll in course
- `GET /api/enrollments/user` - Get user enrollments

### Progress
- `PUT /api/progress/courses/{courseId}/videos/{videoId}/complete` - Mark video complete
- `GET /api/progress/courses/{courseId}` - Get course progress

### Videos (Admin)
- `POST /api/videos` - Upload video
- `DELETE /api/videos/{id}` - Delete video

## 🎨 Features in Detail

### Email Notification System
- **Technology:** SendGrid HTTP API (works on all cloud platforms)
- **Batch Processing:** Sends emails in batches of 100 users
- **Throttling:** 5-second delay between batches to avoid rate limits
- **Async Processing:** Non-blocking email sending using Spring @Async

### Authentication
- **JWT Tokens:** Secure token-based authentication
- **OTP Verification:** Email-based 6-digit OTP with 10-minute expiry
- **Google OAuth:** Single sign-on with Google accounts
- **Password Security:** BCrypt hashing for passwords

### Course Progress
- **Video Tracking:** Marks videos as complete when user finishes
- **Progress Calculation:** Shows completion percentage
- **Sequential Access:** Unlock next video after completing previous one

## 🌐 Deployment

### Backend (Render)
1. Connect GitHub repository to Render
2. Configure environment variables
3. Deploy as Docker container
4. Access at: `https://your-app.onrender.com`

### Frontend (Netlify)
1. Build production bundle: `ng build`
2. Drag and drop `dist/student-study-app/browser` folder to Netlify
3. Configure redirects for Angular routing
4. Access at: `https://your-app.netlify.app`

## 📧 SendGrid Setup

1. Create free account at https://sendgrid.com
2. Verify sender email address
3. Generate API key with Mail Send permissions
4. Add to environment variables
5. **Important:** Sender email must be verified in SendGrid!

## 🔒 Security Features

- JWT token authentication
- Password encryption with BCrypt
- CORS configuration
- Role-based access control
- Email verification
- Secure API endpoints

## 🐛 Troubleshooting

### Email Not Sending (403 Error)
- Verify sender email in SendGrid dashboard
- Check API key is correct
- Ensure API key has Mail Send permissions

### CORS Errors
- Check backend CORS configuration
- Verify frontend is using correct backend URL

### MongoDB Connection Issues
- Check connection string format
- Verify IP whitelist in MongoDB Atlas
- Ensure database user has proper permissions

## 👤 Author

**Akash**
- GitHub: [@Akash000-ui](https://github.com/Akash000-ui)

## 🙏 Acknowledgments

- Spring Boot for robust backend framework
- Angular for modern frontend development
- SendGrid for reliable email delivery
- MongoDB Atlas for cloud database
- Render for backend hosting
- Netlify for frontend hosting
```

Copy and paste this into your GitHub repository's README.md file!Copy and paste this into your GitHub repository's README.md file!
