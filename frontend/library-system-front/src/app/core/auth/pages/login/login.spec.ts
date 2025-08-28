import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Login } from './login';
import { provideRouter } from '@angular/router';
import { AuthStore } from '../../services/auth-store';

describe('Login', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  let authStoreSpy: jasmine.SpyObj<AuthStore>;

  beforeEach(async () => {
    authStoreSpy = jasmine.createSpyObj('AuthStore', [
      'setUsername',
      'setRole',
    ]);
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        provideRouter([]),
        { provide: AuthStore, useValue: authStoreSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should be invalid on load', () => {
    expect(component.form.valid).toBeFalse();
  });

  it('should be valid with right values', () => {
    component.form.setValue({
      email: 'user@example.com',
      password: 'Zaq12wsx!@',
    });
    expect(component.form.valid).toBeTrue();
  });

  it('should set credntialsValid = true and call AuthStore with right values', () => {
    component.form.setValue({
      email: 'user@example.com',
      password: 'Zaq12wsx!@',
    });

    component.onSubmit();

    expect(authStoreSpy.setUsername).toHaveBeenCalledWith('user@example.com');
    expect(authStoreSpy.setRole).toHaveBeenCalledWith('user');
    expect(component.credentialsValid).toBeTrue();
  });

  it('should set credentialsValid = false with wrong values and not call AuthStore', () => {
    component.form.setValue({
      email: 'wrong@example.com',
      password: 'wrongpassword',
    });

    component.onSubmit();

    expect(authStoreSpy.setUsername).not.toHaveBeenCalled();
    expect(authStoreSpy.setRole).not.toHaveBeenCalled();
    expect(component.credentialsValid).toBeFalse();
  });
});
