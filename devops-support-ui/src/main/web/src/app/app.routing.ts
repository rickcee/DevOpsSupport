
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { UnauthorizedComponent } from './unauthorized.component';
import { NotFoundComponent } from './notfound.component';
import { LoginComponent } from './components/login/login.component';
import { SecuredComponent } from './components/secured/secured.component';

const routes: Routes = [
  {
    path: '',
    children: [
      { path: '', redirectTo: 'login', pathMatch: 'full' },
      { path: 'secured', component: SecuredComponent },
      { path: 'login', component: LoginComponent },
      { path: 'unauthorized', component: UnauthorizedComponent },
      { path: 'notfound', component: NotFoundComponent },
      { path: '**', component: NotFoundComponent },
    ]
  }
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, { relativeLinkResolution: 'legacy' }),
  ],
  exports: [
    RouterModule,
  ],
  providers: []
})
export class AppRoutingModule { }
