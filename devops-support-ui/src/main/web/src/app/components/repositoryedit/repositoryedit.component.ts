import { VCSRepository, AppService, VCSRepositoryType } from "app/app.service";
import { Component, ViewChild, Output, EventEmitter } from "@angular/core";
import { SSL_OP_PKCS1_CHECK_1 } from "constants";

@Component({
  selector: 'app-repositoryedit',
  templateUrl: './repositoryedit.component.html',
  styleUrls: ['./repositoryedit.component.css']
})
export class RepositoryeditComponent {

  @Output() onOK: EventEmitter<VCSRepository> = new EventEmitter<VCSRepository>();
   
  show = false;
 
  repositoryToEdit: VCSRepository;
  repositoryTypeData : VCSRepositoryType[];
  
  constructor(private _service: AppService) { }

  ngOnInit() {
    console.log('ngOnInit() - Repo Types...');
    this._service.getRepoTypes().subscribe(resp => {
      this.repositoryTypeData = resp.body;
  },
      error => {
          console.log(error, "error");
      }); 
  }

  open(repo: VCSRepository) {
    console.log("Open....")
    this.show = true;
    this.repositoryToEdit = { ...repo };
  }
 
  close() {
    this.show = false;
  }

  compareRepoType(r1 : VCSRepositoryType, r2: VCSRepositoryType) {
    console.log("compareRepoType");
    return r1 && r2 ? r1.id === r2.id : r1 === r2;
  }
 
  // onKeyPress(event) {
  //   if (event.keyCode === 13) {
  //     this.onOK.emit(this.repositoryToEdit);
  //   }
  // }
 
  onSubmit() {
    this._service.saveRepo(this.repositoryToEdit).subscribe(repo => {
      this.onOK.emit(repo);
    }), error => { console.log('Error Saving Repository')};
    this.close();
  }

}
