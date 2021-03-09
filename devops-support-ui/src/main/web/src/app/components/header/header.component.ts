import { Component, OnInit } from '@angular/core';
import { AppService } from 'app/app.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {

  constructor(private _appSvc : AppService) { }

  ngOnInit() {
  }

  public isRole(role : string) : boolean {
    if(JSON.stringify(this._appSvc.getUserRoles()).indexOf(role) != -1) {
      return true;
    }
    return false;
  }
}
