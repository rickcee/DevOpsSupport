import { Component } from '@angular/core';

@Component({
  selector: 'app-unauthorized',
  template: `
    <h3 style='color: black'> You don't have permissions to run this application.</h3>
  `,
  styles: [],
})
export class UnauthorizedComponent {
}
