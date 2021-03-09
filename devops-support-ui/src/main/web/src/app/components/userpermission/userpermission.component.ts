import { Component, OnInit, ViewChildren, QueryList, AfterViewInit, ViewChild } from '@angular/core';
import { AppService, VCSRepository, UserPermission } from 'app/app.service';
import { UserpermissionaddComponent } from 'app/components/userpermissionadd/userpermissionadd.component';
import { NotificationsService, NotificationType } from 'angular2-notifications';

@Component({
  selector: 'app-userpermission',
  templateUrl: './userpermission.component.html',
  styleUrls: ['./userpermission.component.css']
})
export class UserpermissionComponent {

  //@ViewChildren(UserpermissionaddComponent) modals: QueryList<UserpermissionaddComponent>;

  usernameToQuery : string;
  displayError : boolean = false;
  selectedRepo : VCSRepository;
  data: UserPermission;

  @ViewChild(UserpermissionaddComponent, { static: true }) modal: UserpermissionaddComponent;

  constructor(private _service: AppService, private _notifications: NotificationsService) { }

  getUserPermissions() {
    this._service.getUserPermissions(this.usernameToQuery).subscribe(resp => {
      console.log("Repositories: " + resp.body);
      this.displayError = false;
      this.data = resp.body;
    },
      error => {
        console.log(error, "error");
        this.data = null;
        this.displayError = true;
        //alert('User not found!');
      });
  }

  removePermission() {
    console.log("Removing Permission: " + this.data.username + " / " + this.selectedRepo.id);
    this._service.removeUserPermission(this.data.username, this.selectedRepo.id).subscribe(resp => {
      this._notifications.create('Success', 'Permission Removed.', NotificationType.Success, {
        timeOut: 5000,
        showProgressBar: true,
        pauseOnHover: true,
        clickToClose: true
      });
    },
      error => {
        this._notifications.create('Error', 'An unexpected error has ocurred: ' + (error.message || error), NotificationType.Error, {
          timeOut: 7500,
          showProgressBar: true,
          pauseOnHover: true,
          clickToClose: true
        });
      });
    this.getUserPermissions();
  }

}
