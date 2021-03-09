import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

import { ClarityModule } from '@clr/angular';
import { SecuredComponent } from './components/secured/secured.component';
import { AuthGuardService } from './auth-guard.service';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ManagementComponent } from './components/management/management.component';
import { AuditComponent } from './components/audit/audit.component';
import { HeaderComponent } from './components/header/header.component';
import { AppService } from './app.service';
import { UserpermissionComponent } from './components/userpermission/userpermission.component';
import { EnvironmentComponent } from './components/environment/environment.component';
import { RepositoryComponent } from './components/repository/repository.component';

const routes: Routes = [
    {
      path: 'secured',
      component: SecuredComponent,
      //canActivate: [ AuthGuardService ],
      children: [
        { path: 'dashboard', component: DashboardComponent, canActivate: [ AuthGuardService ], data: { roles: ['ROLE_ADMIN','ROLE_RO','ROLE_AUDIT'] } },
        { path: 'management', component: ManagementComponent, canActivate: [ AuthGuardService ], data: { roles: ['ROLE_ADMIN'] },
            children: [
                { path: 'permissions', component: UserpermissionComponent },
                { path: 'environments', component: EnvironmentComponent },
                { path: 'repositories', component: RepositoryComponent },    
            ]           
        },
        { path: 'audit', component: AuditComponent, canActivate: [ AuthGuardService ], data: { roles: ['ROLE_AUDIT','ROLE_ADMIN'] } },    
      ]
    }
  ];

//   const managementRoutes: Routes = [
//     {
//       path: 'management',
//       component: ManagementComponent,
//       //canActivate: [ AuthGuardService ],
//       children: [
//         { path: 'permissions', component: UserpermissionComponent },
//         { path: 'environment', component: EnvironmentComponent },
//         { path: 'repositories', component: RepositoryComponent },    
//       ]
//     }
//   ];

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    ClarityModule,
    RouterModule.forChild(routes),
    //RouterModule.forChild(managementRoutes),
  ],
  declarations: [
  ],
  providers: [  ],
  exports: [
    //LayoutComponent,
  ]
})
export class UiModule { }
