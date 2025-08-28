import { TestBed } from '@angular/core/testing';

import { AuthStore } from './auth-store';

describe('AuthStore', () => {
  let service: AuthStore;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthStore);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should set username for right value', () => {
    service.setUsername('user@example.com');
    expect(service.username() === 'user@example.com').toBeTrue();
  });

  it('should set role for right value', () => {
    service.setRole('user');
    expect(service.role() === 'user').toBeTrue();
  });

  it('should set isLoggedIn for right value', () => {
    service.setIsLoggenIn(true);
    expect(service.isLoggedIn()).toBeTrue();
  });
});
