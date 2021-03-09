import { Component, OnInit, EventEmitter, Output } from '@angular/core';
import { AppService, Environment } from 'app/app.service';

@Component({
  selector: 'app-environment-edit',
  templateUrl: './environment.edit.component.html',
  styleUrls: ['./environment.edit.component.css']
})
export class EnvironmentEditComponent implements OnInit {
  @Output() onOK: EventEmitter<Environment> = new EventEmitter<Environment>();
   
  show = false;
 
  envToEdit: Environment;

  constructor( private _service: AppService ) { }

  ngOnInit() {

  }

  open(env: Environment) {
    this.show = true;
    this.envToEdit = { ...env };
  }
 
  close() {
    this.show = false;
  }
 
  onSubmit() {
    console.log("Saving: " + this.envToEdit);
    //this._service.saveEnv(this.envToEdit);
    //this._service.getEnvironment();
    //this.close();
    this._service.saveEnv(this.envToEdit).subscribe(env => {
      this.onOK.emit(env);
    }), error => { console.log('Error Saving Environment')};
    this.close();    
  }
}
