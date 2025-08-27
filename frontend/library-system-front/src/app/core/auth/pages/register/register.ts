import { Component } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule],
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
    return this.form.controls.name.invalid;
  }

  get lastNameIsInvalid() {
    return this.form.controls.lastName.invalid;
  }

  get emailIsInvalid() {
    return this.form.controls.email.invalid;
  }

  get passwordIsInvalid() {
    return this.form.controls.password.invalid;
  }

  onSubmit() {
    if (this.form.invalid) {
      console.log('Email invalid: ' + this.emailIsInvalid);
      console.log('Password invalid: ' + this.passwordIsInvalid);
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
