import { Component, OnInit } from '@angular/core';
import { AppService } from 'app/app.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  errorLoggingIn : boolean = false;
  credentials = {username: 'approver', password: 'approver'};

  constructor(private app: AppService, private router: Router) {
  }

  login() {
    console.log("Authenticate: " + JSON.stringify(this.credentials));
    this.app.authenticate(this.credentials, () => {
        console.log("User Authenticated. Rerouting...");
        this.router.navigateByUrl('/secured/dashboard');
    }, () => { console.log("Error Callback..."); this.errorLoggingIn = true; } );
    return false;
  }
  
  ngOnInit() {
  }

}
