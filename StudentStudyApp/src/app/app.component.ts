import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  standalone: false,
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'StudyHub';
  isLoggedIn = false;
  isAdmin = false;
  isOnDashboard = false;
  isOnAdminDashboard = false;
  currentUser: any = null;
  private authSubscription!: Subscription;
  private routerSubscription!: Subscription;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    // Initialize with current authentication state immediately
    this.isLoggedIn = this.authService.isAuthenticated();
    this.isAdmin = this.isLoggedIn ? this.authService.isAdmin() : false;
    this.currentUser = this.authService.getCurrentUser();
  }

  ngOnInit(): void {
    // Subscribe to authentication state changes first
    this.authSubscription = this.authService.isLoggedIn$.subscribe(
      (loggedIn: boolean) => {
        this.isLoggedIn = loggedIn;
        this.isAdmin = loggedIn ? this.authService.isAdmin() : false;
        this.currentUser = loggedIn ? this.authService.getCurrentUser() : null;
      }
    );

    // Subscribe to router events (only NavigationEnd events)
    this.routerSubscription = this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      const url = event.url;
      this.isOnDashboard = url.startsWith('/dashboard');
      this.isOnAdminDashboard = url.startsWith('/admin');
    });

    // Initialize current route state
    const currentUrl = this.router.url;
    this.isOnDashboard = currentUrl.startsWith('/dashboard');
    this.isOnAdminDashboard = currentUrl.startsWith('/admin');
  }

  ngOnDestroy(): void {
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }
  }

  getUserInitials(): string {
    if (!this.currentUser || !this.currentUser.username) {
      return 'U';
    }
    const username = this.currentUser.username.trim();
    const words = username.split(' ');

    if (words.length >= 2) {
      // If name has multiple words, take first letter of first two words
      return (words[0][0] + words[1][0]).toUpperCase();
    } else {
      // If single word, take first two letters
      return username.substring(0, 2).toUpperCase();
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
