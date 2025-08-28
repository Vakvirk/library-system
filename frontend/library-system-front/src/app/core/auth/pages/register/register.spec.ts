import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Register } from './register';
import { provideRouter } from '@angular/router';

describe('Register', () => {
  let component: Register;
  let fixture: ComponentFixture<Register>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Register],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Register);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should be empty (invalid) on load', () => {
    expect(component.form.invalid).toBeTrue();
  });

  it('should be valid with right values', () => {
    component.form.setValue({
      name: 'Adam',
      lastName: 'Kowalski',
      email: 'test2@example.com',
      password: 'TesT123!*',
    });
    expect(component.form.valid).toBeTrue();
  });

  it('should reject short password', () => {
    component.form.controls.password.setValue('Ab1!');
    expect(component.form.controls.password.invalid).toBeTrue();
  });

  it('should reject password without digit', () => {
    component.form.controls.password.setValue('TestTest!@#');
    expect(component.form.controls.password.invalid).toBeTrue();
  });
  it('should reject password without capital letter', () => {
    component.form.controls.password.setValue('testtest123!@#');
    expect(component.form.controls.password.invalid).toBeTrue();
  });
  it('should reject password without special character', () => {
    component.form.controls.password.setValue('TesTTesT123321');
    expect(component.form.controls.password.invalid).toBeTrue();
  });

  it('should accept correct password', () => {
    component.form.controls.password.setValue('Test123!*');
    expect(component.form.controls.password.valid).toBeTrue();
  });

  it('should reject incorrect email address', () => {
    component.form.controls.email.setValue('testexample');
    expect(component.form.controls.email.invalid).toBeTrue();
  });

  // it('should reject not unique email', () => {
  //   component.form.controls.email.setValue('test@example.com');
  //   expect(component.form.controls.email.invalid).toBeTrue();
  // });

  it('should accept correct email', () => {
    component.form.controls.email.setValue('test2@example.com');
    expect(component.form.controls.email.valid).toBeTrue();
  });

  it('should log "Form is inavalid" whith submiting invalid submit', () => {
    spyOn(console, 'log');

    component.onSubmit();

    expect(console.log).toHaveBeenCalledWith('Form is invalid');
  });

  it('should log user data with valid submit', () => {
    spyOn(console, 'log');

    component.form.setValue({
      name: 'Jan',
      lastName: 'Kowalski',
      email: 'test123@example.com',
      password: 'Test123!',
    });

    component.onSubmit();

    expect(console.log).toHaveBeenCalledWith([
      component.form.controls.name.value,
      component.form.controls.lastName.value,
      component.form.controls.email.value,
      component.form.controls.password.value,
    ]);
  });
});
