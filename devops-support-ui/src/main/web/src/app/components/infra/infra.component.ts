import { Component } from '@angular/core';
import { AppService, VCSRepository } from 'app/app.service';

@Component({
    selector: 'app-infra',
    templateUrl: 'infra.component.html',
    styles: []
})
export class InfraComponent {
    data: VCSRepository[] = [];

    constructor(private _service: AppService) { }

    ngOnInit() {

        this._service.getRepositories().subscribe(resp => {
            console.log("Repositories: " + resp.body);
            this.data = resp.body;
        },
            error => {
                console.log(error, "error");
            });

    }
}
