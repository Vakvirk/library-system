import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class AuthStore {
  username = signal<string | null>(null);
  role = signal<string | null>(null);

  setUsername(name: string | null) {
    this.username.set(name);
  }

  setRole(role: string | null) {
    this.role.set(role);
  }
}
