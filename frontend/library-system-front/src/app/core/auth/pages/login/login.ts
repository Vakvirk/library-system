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

  onSubmit() {
    if (this.form.valid) {
      this.authService.setUsername(this.form.controls.email.value);
      this.authService.setRole('user');
      this.credentialsValid = true;

      console.log(this.authService.username());
    } else {
      this.credentialsValid = false;
    }
  }
}
