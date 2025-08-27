import { Component, computed, inject } from '@angular/core';
import { AuthStore } from '../../auth/services/auth-store';

@Component({
  selector: 'app-navbar',
  imports: [],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  authService = inject(AuthStore);
  role = computed(() => this.authService.role());
  email = computed(() => this.authService.username());
}
