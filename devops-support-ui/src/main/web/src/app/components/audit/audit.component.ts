import { Component, AfterViewInit } from '@angular/core';
import { AppService, VCSRepository, AuditEntry, BuildDetails } from 'app/app.service';
import { ClrDatagridComparatorInterface, ClrDatagridSortOrder } from '@clr/angular';
import { NgxSpinnerService } from "ngx-spinner";

@Component({
    selector: 'app-audit',
    templateUrl: 'audit.component.html',
    styleUrls: [ 'audit.component.css' ]
})
export class AuditComponent {
    buildEntries : BuildDetails[] = [];
    auditEntries : AuditEntry[] = [];
    selectedBuild : BuildDetails = null;
    //selectedEntry : AuditEntry = null;
    searchParams : SearchBuildParams = { buildNumber : '', buildDateTo : null, buildDateFrom : null, finalized : false, buildDescription : '', approvedBy : '', triggeredBy : 'rickcee' };
    public auditDateComparator : DateComparator = new DateComparator();
    auditSort = ClrDatagridSortOrder.ASC;
    loadingBuilds : boolean = false;
    loadingAudit : boolean = false;
    
    constructor(private _service: AppService, private spinner: NgxSpinnerService) { }

    ngOnInit() {
        // this.spinner.show();
        // this._service.getAllBuilds().subscribe(resp => {
        //   this.spinner.hide();
        //   console.log("All Builds: " + resp.body);
        //   this.buildEntries = resp.body;
        // },
        // error => {
        //   this.spinner.hide();
        //   console.log(error, "Error getting ALL builds.");
        // });
    }

    searchAuditEntries() {
      this.loadingAudit = true;
      this._service.getBuildDetails(this.selectedBuild.jobId, this.selectedBuild.buildNumber).subscribe(resp => {
        this.loadingAudit = false;
        console.log("Build Details: " + resp.body);
        this.auditEntries = resp.body.auditEntries;
      },
        error => {
          this.loadingAudit = false;
          console.log(error, "Error getting BUILD details.");
        });
    }

    searchBuilds() {
      this.loadingBuilds = true;
      this._service.getBuildEntries(this.searchParams).subscribe(resp => {
        this.loadingBuilds = false;
        console.log("Build Details: " + resp.body);
        this.buildEntries = resp.body;
        this.selectedBuild = null;
      },
        error => {
          this.loadingBuilds = false;
          console.log(error, "Error getting BUILD details.");
        });
    }

    downloadPdf() {
      var url = this._service.getAuditBaseUrl() + this.selectedBuild.jobId + '/' + this.selectedBuild.buildNumber;
      console.log("Opening PDF window: [" + url + "]");
      window.open(url, '_blank');
      console.log("After downloadPdf()");
    }

}

class DateComparator implements ClrDatagridComparatorInterface<AuditEntry> {
  compare(a: AuditEntry, b: AuditEntry) {
      console.log("Comparing...");
      return a.id - b.id;
  }
}

export class SearchBuildParams {
  buildNumber : string;
  buildDescription : string;
  buildDateFrom : Date;
  buildDateTo : Date;
  finalized : boolean = false;
  triggeredBy : string;
  approvedBy : string;
}
