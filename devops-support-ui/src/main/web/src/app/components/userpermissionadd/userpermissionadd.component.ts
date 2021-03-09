import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { AppService, Environment, VCSRepository } from 'app/app.service';
import { NotificationsService, NotificationType } from 'angular2-notifications';

@Component({
  selector: 'app-userpermissionadd',
  templateUrl: './userpermissionadd.component.html',
  styleUrls: ['./userpermissionadd.component.css']
})
export class UserpermissionaddComponent {
  @Output() onOK: EventEmitter<Environment> = new EventEmitter<Environment>();

  username : string;
  show = false;
  repositoryData : VCSRepository[];
  selectedRepo : VCSRepository;
  
  constructor( private _service: AppService, private _notifications: NotificationsService ) { }

  ngOnInit() {
    console.log('ngOnInit()');
    this._service.getRepositories().subscribe(resp => {
      console.log('ngOnInit() - Got Data!');
      this.repositoryData = resp.body;
  },
      error => {
          console.log("Error loading repositories. " + error);
      }); 
  }

  open() {
    this.show = true;
    this.username = '';
    this.selectedRepo = null;
  }
 
  close() {
    this.show = false;
  }
 
  onSubmit() {
    console.log("SelectedRepo: " + JSON.stringify(this.selectedRepo));
    this._service.addUserPermission(this.username, this.selectedRepo.id).subscribe(resp => {
      this._notifications.create('Success', 'Permission Saved.', NotificationType.Success, {
        timeOut: 5000,
        showProgressBar: true,
        pauseOnHover: true,
        clickToClose: true
      });
      this.close();
    },
      error => {
        console.log(error);
        this._notifications.create('Error', 'An unexpected error has ocurred: ' + (error.message || error), NotificationType.Error, {
          timeOut: 7500,
          showProgressBar: true,
          pauseOnHover: true,
          clickToClose: true
        });
      });
  }
}
