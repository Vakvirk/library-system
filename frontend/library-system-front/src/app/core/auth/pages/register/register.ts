import { Component } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  form = new FormGroup({
    name: new FormControl('', {
      validators: [Validators.required],
    }),
    lastName: new FormControl('', {
      validators: [Validators.required],
    }),
    email: new FormControl('', {
      validators: [Validators.email, Validators.required],
    }),
    password: new FormControl('', {
      validators: [Validators.minLength(8), Validators.required],
    }),
  });

  get nameIsInvalid() {
    return this.form.controls.name.invalid && this.form.controls.name.touched;
  }

  get lastNameIsInvalid() {
    return (
      this.form.controls.lastName.invalid && this.form.controls.lastName.touched
    );
  }

  get emailIsInvalid() {
    return (
      this.form.controls.email.invalid &&
      this.form.controls.email.dirty &&
      this.form.controls.email.touched
    );
  }

  get passwordIsInvalid() {
    return (
      this.form.controls.password.invalid &&
      this.form.controls.password.dirty &&
      this.form.controls.password.touched
    );
  }

  onSubmit() {
    if (this.form.invalid) {
      console.log('Form is invalid');
    } else {
      const data = [
        this.form.value.name,
        this.form.value.lastName,
        this.form.value.email,
        this.form.value.password,
      ];
      console.log(this.form.invalid);
      console.log(this.form.controls.password);
      console.log(data);
    }
  }
}
