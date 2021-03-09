import { Injectable } from '@angular/core';

import { AppService } from './app.service';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot, CanActivate } from '@angular/router';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthGuardService implements CanActivate {

  constructor(private appSvc : AppService, private _router: Router) { console.log('AuthGuardService - Constructor'); }

  canActivate(next: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    console.log('AuthGuardService - canActivate(): ' + this.appSvc.isUserLoggedIn()); 

    /* Check if the User is logged in */
    if(!this.appSvc.isUserLoggedIn()) {
      console.log("User Not Logged In.");
      this._router.navigateByUrl('login');
      return false;
    }

    /* Check the User Roles */
    let roles : Object[] = this.appSvc.getUserRoles();
    console.log('User Roles: ' + JSON.stringify(roles));
    for (var i = 0; i < roles.length; i++) {
      for (var j=0; j < next.data.roles.length; j++) {
        console.log('Searching for [' + next.data.roles[j] + '] in ' + JSON.stringify(roles[i]) + " : " + JSON.stringify(roles[i]).indexOf(next.data.roles[j]));
        if ( roles[i] && JSON.stringify(roles[i]).indexOf(next.data.roles[j]) > 0) {
          return true;
        }
      }
    }

    return false;
  }
  
}
