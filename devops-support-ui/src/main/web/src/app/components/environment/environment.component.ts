import { Component, OnInit, ViewChildren, QueryList, ViewChild } from '@angular/core';
import { Environment, AppService } from 'app/app.service';
import { EnvironmentEditComponent } from 'app/components/environmentedit/environment.edit.component';

@Component({
  selector: 'app-environment',
  templateUrl: './environment.component.html',
  styleUrls: ['./environment.component.css']
})
export class EnvironmentComponent implements OnInit {
  environmentData : Environment[] = [];
  selectedEnv : Environment;
  loadingEnv : boolean = false;

  @ViewChild('editEnv', { static: true }) editEnvModal: EnvironmentEditComponent;

  //@ViewChildren("editEnv")
  //public Grids: QueryList<EnvironmentEditComponent>

  constructor(private _service: AppService) { }

  ngOnInit() {
    console.log("EnvironmentEditComponent Modal: " + this.editEnvModal);
    this.refreshEnvironments();
  }

  refreshEnvironments() : void {
    this.loadingEnv = true;
    this._service.getEnvironment().subscribe(resp => {
      this.loadingEnv = false;
      console.log("Environment: " + resp.body);
      this.environmentData = resp.body;
      },
      error => {
        this.loadingEnv = false;
        console.log(error, "error");
      }); 
  }

  ngAfterViewInit(): void {
    console.log("ngAfterViewInit...");
    // this.Grids.changes.subscribe((comps: QueryList <EnvironmentEditComponent>) =>
    // {
    //     console.log("EnvironmentEditComponent: [" + comps + "]");
    //     if(comps.length > 0) {
    //         this.modal = comps.first;
    //     }
    // });
    this.editEnvModal.onOK.subscribe( repo => {
      console.log("onOK");
      this.refreshEnvironments();
      //this.editRepoModal.close();
    });    
  }
}
