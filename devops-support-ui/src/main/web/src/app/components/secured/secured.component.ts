import { Component, OnInit } from '@angular/core';
import { AuthGuardService } from 'app/auth-guard.service';
import { Routes } from '@angular/router';
import { DashboardComponent } from '../dashboard/dashboard.component';
import { ManagementComponent } from '../management/management.component';
import { AuditComponent } from '../audit/audit.component';

@Component({
  selector: 'app-secured',
  templateUrl: './secured.component.html',
  styleUrls: ['./secured.component.css']
})
export class SecuredComponent implements OnInit {

  constructor() { }

  ngOnInit() {
  }

}
