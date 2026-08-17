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
import { AboutComponent } from './components/public/about/about.component';
import { ServicesComponent } from './components/public/services/services.component';
import { ProjectsComponent } from './components/public/projects/projects.component';
import { InternshipComponent } from './components/public/internship/internship.component';
import { ContactComponent } from './components/public/contact/contact.component';
import { PrivacyPolicyComponent } from './components/public/privacy-policy/privacy-policy.component';
import { TermsComponent } from './components/public/terms/terms.component';
import { CheckoutComponent } from './components/payment/checkout/checkout.component';
import { authGuard } from './guards/auth.guard';
import { adminGuard } from './guards/admin.guard';
import { CouponManagementComponent } from './components/admin/coupon-management/coupon-management.component';

const routes: Routes = [
  {
    path: '',
    component: HomeComponent,
    data: {
      seo: {
        title: 'VOIDMAIN ACADEMY | Software Courses Training in Hyderabad',
        description: 'Join VOIDMAIN ACADEMY in Dilsukhnagar, Hyderabad for Java full stack, Python full stack, AI, data science, analytics, internships, and academic project guidance.',
        keywords: 'software courses training Hyderabad, Java full stack course Dilsukhnagar, Python full stack training Hyderabad, AI course Hyderabad, data science course Hyderabad, IEEE projects Hyderabad, Voidmain Academy',
        canonical: '/'
      }
    }
  },
  {
    path: 'home',
    component: HomeComponent,
    data: {
      seo: {
        title: 'VOIDMAIN ACADEMY | Software Courses Training in Hyderabad',
        description: 'Join VOIDMAIN ACADEMY in Dilsukhnagar, Hyderabad for Java full stack, Python full stack, AI, data science, analytics, internships, and academic project guidance.',
        keywords: 'software courses training Hyderabad, Java full stack course Dilsukhnagar, Python full stack training Hyderabad, AI course Hyderabad, data science course Hyderabad, IEEE projects Hyderabad, Voidmain Academy',
        canonical: '/'
      }
    }
  },
  {
    path: 'courses',
    component: CourseCatalogComponent,
    data: {
      seo: {
        title: 'Software Courses in Hyderabad | Java, Python, AI, Data Science',
        description: 'Explore classroom and online software courses at VOIDMAIN ACADEMY: Java full stack, Python full stack, AI, data science, data analytics, web development, SAP, and Tally.',
        keywords: 'software courses Hyderabad, online software courses, Java full stack training, Python full stack course, AI training Hyderabad, data science training, data analytics course, SAP Tally training',
        canonical: '/courses'
      }
    }
  },
  {
    path: 'courses/preview/:id',
    component: CoursePreviewComponent,
    data: {
      seo: {
        title: 'Course Preview | VOIDMAIN ACADEMY',
        description: 'Preview course details, trainer information, language, duration, and enrollment options at VOIDMAIN ACADEMY.',
        keywords: 'course preview Voidmain Academy, software course details Hyderabad, online course preview',
        canonical: '/courses',
        type: 'article'
      }
    }
  },
  {
    path: 'about',
    component: AboutComponent,
    data: {
      seo: {
        title: 'About VOIDMAIN ACADEMY | Software Training Institute Hyderabad',
        description: 'Learn about VOIDMAIN ACADEMY, a Dilsukhnagar Hyderabad software training institute for practical courses, internships, project mentoring, and placement-oriented learning.',
        keywords: 'about Voidmain Academy, software training institute Hyderabad, computer training institute Dilsukhnagar',
        canonical: '/about'
      }
    }
  },
  {
    path: 'services',
    component: ServicesComponent,
    data: {
      seo: {
        title: 'Software Training and Development Services | VOIDMAIN ACADEMY',
        description: 'VOIDMAIN ACADEMY offers software courses, classroom and online training, live project development, academic project support, internships, and placement assistance.',
        keywords: 'software development services Hyderabad, live projects Hyderabad, software training services, project development Dilsukhnagar',
        canonical: '/services'
      }
    }
  },
  {
    path: 'projects',
    component: ProjectsComponent,
    data: {
      seo: {
        title: 'IEEE and Academic Projects in Hyderabad | VOIDMAIN ACADEMY',
        description: 'Get guided IEEE projects, final year projects, mini projects, major projects, documentation, implementation support, and presentation help in Hyderabad.',
        keywords: 'IEEE projects Hyderabad, academic projects Hyderabad, final year project guidance, mini projects, major projects, project documentation Hyderabad',
        canonical: '/projects'
      }
    }
  },
  {
    path: 'internship',
    component: InternshipComponent,
    data: {
      seo: {
        title: 'Software Internship in Hyderabad | Java, Python, AI, Full Stack',
        description: 'Apply for industry-oriented software internships at VOIDMAIN ACADEMY in Java, Python, AI, full stack, data science, Android, and web development.',
        keywords: 'software internship Hyderabad, Java internship Hyderabad, Python internship, AI internship, full stack internship Dilsukhnagar',
        canonical: '/internship'
      }
    }
  },
  {
    path: 'contact',
    component: ContactComponent,
    data: {
      seo: {
        title: 'Contact VOIDMAIN ACADEMY Dilsukhnagar Hyderabad',
        description: 'Contact VOIDMAIN ACADEMY near Dilsukhnagar Metro Station for software courses, internships, academic projects, and development service enquiries.',
        keywords: 'Voidmain Academy contact, software training Dilsukhnagar contact, computer institute Hyderabad phone',
        canonical: '/contact'
      }
    }
  },
  {
    path: 'privacy-policy',
    component: PrivacyPolicyComponent,
    data: {
      seo: {
        title: 'Privacy Policy | VOIDMAIN ACADEMY',
        description: 'Read the VOIDMAIN ACADEMY privacy policy for course, internship, project, and enquiry data handling.',
        keywords: 'Voidmain Academy privacy policy',
        canonical: '/privacy-policy'
      }
    }
  },
  {
    path: 'terms',
    component: TermsComponent,
    data: {
      seo: {
        title: 'Terms and Conditions | VOIDMAIN ACADEMY',
        description: 'Read the VOIDMAIN ACADEMY terms and conditions for courses, internships, project guidance, and software development enquiries.',
        keywords: 'Voidmain Academy terms and conditions',
        canonical: '/terms'
      }
    }
  },
  { path: 'signin', component: SigninComponent, data: { seo: { title: 'Sign In | VOIDMAIN ACADEMY', description: 'Sign in to your VOIDMAIN ACADEMY learning account.', canonical: '/signin', noIndex: true } } },
  { path: 'signup', component: SignupComponent, data: { seo: { title: 'Sign Up | VOIDMAIN ACADEMY', description: 'Create your VOIDMAIN ACADEMY learning account.', canonical: '/signup', noIndex: true } } },
  { path: 'forgot-password', component: ForgotPasswordComponent, data: { seo: { title: 'Forgot Password | VOIDMAIN ACADEMY', description: 'Recover your VOIDMAIN ACADEMY account password.', canonical: '/forgot-password', noIndex: true } } },
  { path: 'dashboard', component: UserDashboardComponent, canActivate: [authGuard], data: { seo: { title: 'Dashboard | VOIDMAIN ACADEMY', noIndex: true } } },
  { path: 'profile', component: UserProfileComponent, canActivate: [authGuard], data: { seo: { title: 'Profile | VOIDMAIN ACADEMY', noIndex: true } } },
  { path: 'course/:id', component: CourseDetailComponent, canActivate: [authGuard], data: { seo: { title: 'Course Learning | VOIDMAIN ACADEMY', noIndex: true } } },
  { path: 'payment/checkout', component: CheckoutComponent, canActivate: [authGuard], data: { seo: { title: 'Checkout | VOIDMAIN ACADEMY', noIndex: true } } },
  { path: 'admin', component: AdminDashboardComponent, canActivate: [adminGuard], data: { seo: { title: 'Admin | VOIDMAIN ACADEMY', noIndex: true } } },
  { path: 'admin/create-course', component: CreateCourseComponent, canActivate: [adminGuard], data: { seo: { title: 'Create Course | VOIDMAIN ACADEMY', noIndex: true } } },
  { path: 'admin/edit-course/:id', component: EditCourseComponent, canActivate: [adminGuard], data: { seo: { title: 'Edit Course | VOIDMAIN ACADEMY', noIndex: true } } },
  { path: 'admin/courses/:courseId/videos', component: VideoManagementComponent, canActivate: [adminGuard], data: { seo: { title: 'Manage Videos | VOIDMAIN ACADEMY', noIndex: true } } },
  { path: 'admin/categories', component: CategoryManagementComponent, canActivate: [adminGuard], data: { seo: { title: 'Manage Categories | VOIDMAIN ACADEMY', noIndex: true } } },
  {
    path: 'admin/coupons',
    component: CouponManagementComponent,
    canActivate: [adminGuard],
    data: { seo: { title: 'Manage Coupons | VOIDMAIN ACADEMY', noIndex: true } }
  },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
