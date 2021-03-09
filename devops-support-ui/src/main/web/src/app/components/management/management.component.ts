import { Component, AfterViewInit, ViewChild, OnChanges, ViewChildren, QueryList, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { AppService, VCSRepository, Environment } from 'app/app.service';
import { ClrDatagrid, ClrDatagridStringFilterInterface } from '@clr/angular';
import { Routes } from '@angular/router';
import { EnvironmentComponent } from '../environment/environment.component';
import { RepositoryComponent } from '../repository/repository.component';
import { UserpermissionComponent } from '../userpermission/userpermission.component';

@Component({
    selector: 'app-management',
    changeDetection: ChangeDetectionStrategy.Default,
    templateUrl: 'management.component.html',
    styleUrls: [ 'management.component.css' ]
})
export class ManagementComponent {
 //implements OnChanges {
 //implements AfterViewInit {

}
