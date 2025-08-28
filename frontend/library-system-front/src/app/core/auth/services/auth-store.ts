import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class AuthStore {
  username = signal<string | null>(null);
  role = signal<string | null>(null);
  isLoggedIn = signal<boolean>(false);

  setUsername(name: string | null) {
    this.username.set(name);
  }

  setRole(role: string | null) {
    this.role.set(role);
  }

  setIsLoggenIn(status: boolean) {
    this.isLoggedIn.set(status);
  }
}
