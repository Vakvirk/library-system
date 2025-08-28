import { Component, inject } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthStore } from '../../services/auth-store';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  credentials = ['user@example.com', 'Zaq12wsx!@'];
  authService = inject(AuthStore);
  credentialsValid: boolean | undefined = undefined;

  form = new FormGroup({
    email: new FormControl('', {
      validators: [Validators.required, Validators.email],
    }),
    password: new FormControl('', {
      validators: [Validators.required],
    }),
  });

  validateCredentials(username: string | null, password: string | null) {
    return username === this.credentials[0] && password === this.credentials[1];
  }

  onSubmit() {
    if (
      this.validateCredentials(
        this.form.controls.email.value,
        this.form.controls.password.value
      )
    ) {
      this.authService.setUsername(this.form.controls.email.value);
      this.authService.setRole('user');
      this.credentialsValid = true;
    } else {
      this.credentialsValid = false;
    }
  }
}
