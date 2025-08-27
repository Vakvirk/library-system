import { Routes } from '@angular/router';
import { Register } from './core/auth/pages/register/register';
import { Login } from './core/auth/pages/login/login';

export const routes: Routes = [
  {
    path: 'register',
    component: Register,
  },
  {
    path: 'login',
    component: Login,
  },
];
