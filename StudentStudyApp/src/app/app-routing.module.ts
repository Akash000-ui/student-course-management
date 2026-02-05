import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { SigninComponent } from './components/auth/signin/signin.component';
import { SignupComponent } from './components/auth/signup/signup.component';
import { ForgotPasswordComponent } from './components/auth/forgot-password/forgot-password.component';
import { UserDashboardComponent } from './components/dashboard/user-dashboard/user-dashboard.component';
import { UserProfileComponent } from './components/dashboard/user-profile/user-profile.component';
import { AdminDashboardComponent } from './components/admin/admin-dashboard/admin-dashboard.component';
import { CreateCourseComponent } from './components/admin/create-course/create-course.component';
import { VideoManagementComponent } from './components/admin/video-management/video-management.component';
import { EditCourseComponent } from './components/admin/edit-course/edit-course.component';
import { CategoryManagementComponent } from './components/admin/category-management/category-management.component';
import { CourseDetailComponent } from './components/course/course-detail/course-detail.component';
import { CourseCatalogComponent } from './components/public/course-catalog/course-catalog.component';
import { CoursePreviewComponent } from './components/public/course-preview/course-preview.component';
import { authGuard } from './guards/auth.guard';
import { adminGuard } from './guards/admin.guard';

const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'home', component: HomeComponent },
  { path: 'courses', component: CourseCatalogComponent }, // Public course catalog
  { path: 'courses/preview/:id', component: CoursePreviewComponent }, // Public course preview
  { path: 'signin', component: SigninComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'dashboard', component: UserDashboardComponent, canActivate: [authGuard] },
  { path: 'profile', component: UserProfileComponent, canActivate: [authGuard] },
  { path: 'course/:id', component: CourseDetailComponent, canActivate: [authGuard] },
  { path: 'admin', component: AdminDashboardComponent, canActivate: [adminGuard] },
  { path: 'admin/create-course', component: CreateCourseComponent, canActivate: [adminGuard] },
  { path: 'admin/edit-course/:id', component: EditCourseComponent, canActivate: [adminGuard] },
  { path: 'admin/courses/:courseId/videos', component: VideoManagementComponent, canActivate: [adminGuard] },
  { path: 'admin/categories', component: CategoryManagementComponent, canActivate: [adminGuard] },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
