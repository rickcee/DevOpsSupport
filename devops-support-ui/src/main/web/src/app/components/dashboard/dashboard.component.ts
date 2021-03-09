import { Component, OnInit } from '@angular/core';
import { ChartOptions, ChartType, ChartDataSets } from 'chart.js';
import { Label, Color } from 'ng2-charts';
import { NotificationsService, NotificationType } from 'angular2-notifications';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  constructor(private _notifications: NotificationsService) { }

  ngOnInit() {
  }

  title = 'bar-chart';
  barChartOptions: ChartOptions = {
    responsive: true,
  };
  barChartLabels: Label[] = ["4/2020", "7/2020", "9/2020", "2/2121", "3/2021"];
  barChartType: ChartType = 'horizontalBar';
  barChartLegend = true;
//   barChartPlugins:any = {'backgroundColor': [
//     "#FF6384",
//  "#4BC0C0",
//  "#FFCE56",
//  "#E7E9ED",
//  "#36A2EB"
//  ]
//}
 public barChartColors: Color[] = [
  { backgroundColor: 'orange' },
]

  barChartData: ChartDataSets[] = [
    { data: [4, 7, 5, 9, 3, 0], label: 'Number of Releases' }
  ];

  public showNotification() {
    this._notifications.create('Error', 'An unexpected error has ocurred. Please contact support.', NotificationType.Error, {
      timeOut: 5000,
      showProgressBar: true,
      pauseOnHover: true,
      clickToClose: true
    });
  }

}
