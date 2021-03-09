import { BrowserModule } from '@angular/platform-browser';
import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { ClarityModule } from '@clr/angular';

import { AppComponent } from './app.component';
import { ManagementComponent } from './components/management/management.component';
import { UnauthorizedComponent } from './unauthorized.component';
//import { UiModule } from './ui/ui.module';
//import { AppRoutingModule } from './app.routing';
//import { PagesModule } from './pages/pages.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
//import { OAuthModule } from 'angular-oauth2-oidc';
import { NotFoundComponent } from './notfound.component';
import { AgGridModule } from 'ag-grid-angular';
import { AuditComponent } from './components/audit/audit.component';
import { InfraComponent } from './components/infra/infra.component';
import { EnvironmentComponent } from './components/environment/environment.component';
import { EnvironmentEditComponent } from './components/environmentedit/environment.edit.component';
import { UserpermissionComponent } from './components/userpermission/userpermission.component';
import { UserpermissionaddComponent } from './components/userpermissionadd/userpermissionadd.component';
import { RepositoryComponent } from './components/repository/repository.component';
import { RepositoryeditComponent } from './components/repositoryedit/repositoryedit.component';
import { LoginComponent } from './components/login/login.component';
import { AlertComponent } from './components/alert/alert.component';
import { HeaderComponent } from './components/header/header.component';
import { AppRoutingModule } from './app.routing';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { SecuredComponent } from './components/secured/secured.component';
import { UiModule } from './ui.module';
import { AppService } from './app.service';
import { AuthGuardService } from './auth-guard.service';
import { ErrorInterceptor } from './ErrorInterceptor';
import { ChartsModule } from 'ng2-charts';
import { SimpleNotificationsModule } from 'angular2-notifications';

@NgModule({
  declarations: [
    AppComponent,
    ManagementComponent,
    AuditComponent,
    InfraComponent,
    UnauthorizedComponent,
    NotFoundComponent,
    EnvironmentComponent,
    EnvironmentEditComponent,
    UserpermissionComponent,
    UserpermissionaddComponent,
    RepositoryComponent,
    RepositoryeditComponent,
    LoginComponent,
    AlertComponent,
    HeaderComponent,
    DashboardComponent,
    SecuredComponent,
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    HttpClientModule,
    ClarityModule,
    //OAuthModule.forRoot(),
    AgGridModule.withComponents([]),
    UiModule,
    //ErrorInterceptor,
    //PagesModule,
    AppRoutingModule,
    ChartsModule,
    SimpleNotificationsModule.forRoot(),
    BrowserAnimationsModule
  ],
  providers: [AppService, AuthGuardService,
                  {provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true}
              ],
  schemas: [ CUSTOM_ELEMENTS_SCHEMA],
  bootstrap: [AppComponent]
})
export class AppModule {

 }
