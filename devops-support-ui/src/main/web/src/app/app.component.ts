import { Component } from '@angular/core';
import { AppService, VCSRepository, BuildDetails, AuditEntry } from './app.service';

@Component({
  /*<router-outlet></router-outlet> */
  selector: 'app-root',
  templateUrl: 'app.component.html',
  providers: [],
  styles: [],
})
export class AppComponent {
  title = 'RickCee\'s DevOps Support UI';

  constructor(private _service: AppService) { }

  ngOnInit() {

  }

}
