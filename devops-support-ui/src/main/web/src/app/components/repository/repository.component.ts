import { Component, AfterViewInit, ViewChild, OnChanges, ViewChildren, QueryList, ChangeDetectionStrategy, ChangeDetectorRef, OnInit } from '@angular/core';
import { AppService, VCSRepository, Environment } from 'app/app.service';
import { ClrDatagrid, ClrDatagridStringFilterInterface } from '@clr/angular';
import { RepositoryeditComponent } from 'app/components/repositoryedit/repositoryedit.component';

@Component({
  selector: 'app-repository',
  templateUrl: './repository.component.html',
  styleUrls: ['./repository.component.css']
})
export class RepositoryComponent implements OnInit {

  private gridApi;
  private gridColumnApi;
  private urlFilter = new UrlFilter();
  private loadingRepo : boolean = false;

  //private modal: RepositoryeditComponent;

  @ViewChild('editRepo', { static: true }) editRepoModal: RepositoryeditComponent;
  @ViewChild('datagridRef', { static: true }) ClDg: ClrDatagrid;

  // @ViewChildren("editRepo")
  // public Grids: QueryList<EditRepoComponent>

  // @ViewChildren('datagridRef') 
  // public ClDg: QueryList<ClrDatagrid>

  data: VCSRepository[] = [];
  //rowData : any;
  selectedRepo : VCSRepository;
  newRepo : VCSRepository = { id: null, url: '', description: '', extAlias : '', fsLocation: '', environmentGroup: '', tagBase: '', disabled : false, groupEmailAddress: 'XXXX@company.com'};

  constructor(private _service: AppService,  private cdref: ChangeDetectorRef) { }

  ngOnInit() {
      console.log("RepositoryComponent Modal: " + this.editRepoModal);
      this.refreshRepositories();
  }

  refreshRepositories() : void {
    this.loadingRepo = true;
    this._service.getRepositories().subscribe(resp => {
      this.loadingRepo = false;
      console.log("Repositories: " + resp.body);
      this.data = resp.body;
      //this.rowData = resp.body;
      console.log("gridApi: " + this.gridApi);
      },
      error => {
        this.loadingRepo = false;
        console.log(error, "error");
      });
  }

  // gridOptions = {
  //     defaultColDef: {
  //       filter: true // set filtering on for all cols
  //     },
  //     columnDefs : [
  //       {headerName: 'ID', field: 'id', filter: true, width: 55 },
  //       {headerName: 'URL', field: 'url', width: 400, filter: "agTextColumnFilter" },
  //       {headerName: 'Description', field: 'description', width: 400 },
  //       {headerName: 'Alias', field: 'extAlias', width: 100, filter: "agTextColumnFilter"},
  //       {headerName: 'Checkout Location', field: 'fsLocation', width: 50, filter: "agTextColumnFilter"},
  //       {headerName: 'Tag Base', field: 'tagBase', width: 100, filter: "agTextColumnFilter"},
  //       {headerName: 'Disabled', field: 'disabled'},
  //     ]  
  // }

  openModal() : void {
    this.editRepoModal.open(this.newRepo);
  }

  ngAfterViewInit(): void {
      console.log("ngAfterViewInit...");

      this.editRepoModal.onOK.subscribe( repo => {
        console.log("onOK");
        this.refreshRepositories();
        //this.editRepoModal.close();
      });

      // this.Grids.changes.subscribe((comps: QueryList <EditRepoComponent>) =>
      // {
      //     console.log("EditRepoComponent: [" + comps + "]");
      //     if(comps.length > 0) {
      //       console.log("comps.first...");
      //       this.modal = comps.first;
      //         this.modal.onOK.subscribe(entry => {
      //            console.log("onOK");
      //            this.refreshRepositories();
      //         //     //this.user = user;
      //         //     console.log('Returned Model: ' + entry.extAlias);
      //         //     this._service.saveRepo(entry);
      //         //     this.modal.close();
      //          });                
      //     }
      // });

      // this.ClDg.changes.subscribe((comps: QueryList <ClrDatagrid>) =>
      // {
      //     console.log("DataGrid: [" + comps + "]");
      //     if(comps.length > 0) {
      //         comps.first.resize();
      //     }
      // });

    }

    onGridReady(params) {
      this.gridApi = params.api;
      this.gridColumnApi = params.columnApi;
      this.gridApi.sizeColumnsToFit();
    }

}

class UrlFilter implements ClrDatagridStringFilterInterface<VCSRepository> {
  accepts(repo: VCSRepository, search: string):boolean {
      console.log("filering...");
      setTimeout(() => { return repo.url.toLowerCase().indexOf(search) >= 0; }, 0);
      return false;
  }
}
