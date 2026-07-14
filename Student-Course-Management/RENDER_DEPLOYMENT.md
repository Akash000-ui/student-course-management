# StudyHub Backend Deployment Guide

## Docker Image
- **Repository**: `akash757/studiehub-backend:latest`
- **Exposed Port**: 8080

## Render Deployment Steps

1. Go to [render.com](https://render.com) and sign up/login
2. Click "New +" → "Web Service"
3. Choose "Deploy an existing image from a registry"
4. Enter image URL: `akash757/studiehub-backend:latest`
5. Configure the service:
   - **Name**: `studiehub-backend`
   - **Region**: Choose closest to your users
   - **Branch**: main (if using Git)
   - **Runtime**: Docker
   - **Build Command**: (leave empty for Docker images)
   - **Start Command**: (leave empty, uses Dockerfile CMD)

## Environment Variables
Set these in Render's Environment section:

```
SPRING_PROFILES_ACTIVE=production
FILE_UPLOAD_DIR=/app/uploads
SPRING_DATA_MONGODB_URI=mongodb+srv://admin:QUBPO7SJqLyaQxkv@student.ijerftf.mongodb.net/studyApp?retryWrites=true&w=majority&appName=Student
JWT_SECRET=mkZoXlqK0jhhiH9hx+X1AXSX35sSa9IcNrLUn3nNJnNyHJM/2oMf1E8JBTGtnDf9Te9En2kAFr9a91AN2WZ2hQ==
JWT_EXPIRATION=3600000
GOOGLE_CLIENTID=1066899334085-pk107ga4netakv1f4j1lv51sd463aiab.apps.googleusercontent.com
```

## Frontend Configuration
Once deployed, you'll get a Render URL like: `https://studiehub-backend-xxx.onrender.com`

Update your Angular frontend services to use this URL instead of `localhost:8080`:

**Files to update in StudentStudyApp:**
- `src/app/services/auth.service.ts`
- `src/app/services/course.service.ts`
- `src/app/services/enrollment.service.ts`
- `src/app/services/progress.service.ts`
- `src/app/services/video.service.ts`

Replace all instances of:
```typescript
private baseUrl = 'http://localhost:8080/api/...';
```

With your Render URL:
```typescript
private baseUrl = 'https://your-render-url.onrender.com/api/...';
```

## Health Check
Render will automatically check `http://your-service:8080/` for health status.
Spring Boot provides actuator endpoints at `/actuator/health` by default.

## Deployment Notes
- First deployment may take 2-3 minutes
- Render's free tier may have some limitations (sleeping after inactivity)
- Consider upgrading to paid tier for production use
- Monitor logs in Render dashboard for any deployment issues

## CORS Configuration
The backend is already configured to accept requests from:
- `http://localhost:4200` (development)
- `https://studiehub.netlify.app` (production frontend)

## Database
Uses MongoDB Atlas (already configured in connection string)
