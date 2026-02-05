import { NgModule } from '@angular/core';
import { BrowserModule, provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpClientModule, provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { CommonModule, DatePipe } from '@angular/common';
import { authInterceptor } from './interceptors/auth.interceptor';

// Angular Material Imports
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatChipsModule } from '@angular/material/chips';
import { MatSelectModule } from '@angular/material/select';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatBadgeModule } from '@angular/material/badge';
import { MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDividerModule } from '@angular/material/divider';
import { DragDropModule } from '@angular/cdk/drag-drop';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HomeComponent } from './components/home/home.component';
import { SigninComponent } from './components/auth/signin/signin.component';
import { SignupComponent } from './components/auth/signup/signup.component';
import { ForgotPasswordComponent } from './components/auth/forgot-password/forgot-password.component';
import { UserDashboardComponent } from './components/dashboard/user-dashboard/user-dashboard.component';
import { CourseCardComponent } from './components/course/course-card/course-card.component';
import { AdminDashboardComponent } from './components/admin/admin-dashboard/admin-dashboard.component';
import { CourseManagementComponent } from './components/admin/course-management/course-management.component';
import { VideoManagementComponent } from './components/admin/video-management/video-management.component';
import { CreateCourseComponent } from './components/admin/create-course/create-course.component';
import { CourseDetailComponent } from './components/course/course-detail/course-detail.component';
import { EditCourseComponent } from './components/admin/edit-course/edit-course.component';
import { CourseCatalogComponent } from './components/public/course-catalog/course-catalog.component';
import { CoursePreviewComponent } from './components/public/course-preview/course-preview.component';
import { UserProfileComponent } from './components/dashboard/user-profile/user-profile.component';
import { CategoryManagementComponent } from './components/admin/category-management/category-management.component';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    SigninComponent,
    SignupComponent,
    ForgotPasswordComponent,
    UserDashboardComponent,
    CourseCardComponent,
    AdminDashboardComponent,
    CourseManagementComponent,
    VideoManagementComponent,
    CreateCourseComponent,
    CourseDetailComponent,
    EditCourseComponent,
    CourseCatalogComponent,
    CoursePreviewComponent,
    UserProfileComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    CategoryManagementComponent,
    BrowserAnimationsModule,
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatGridListModule,
    MatChipsModule,
    MatSelectModule,
    MatMenuModule,
    MatSidenavModule,
    MatListModule,
    MatPaginatorModule,
    MatBadgeModule,
    MatDialogModule,
    MatTooltipModule,
    MatProgressBarModule,
    MatCheckboxModule,
    MatDividerModule,
    DragDropModule
  ],
  providers: [
    DatePipe,
    provideClientHydration(withEventReplay()),
    provideHttpClient(withFetch(), withInterceptors([authInterceptor]))
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
