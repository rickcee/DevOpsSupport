import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { EnvironmentEditComponent } from './environment.edit.component';;

describe('EnvironmentEditComponent', () => {
  let component: EnvironmentEditComponent;
  let fixture: ComponentFixture<EnvironmentEditComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ EnvironmentEditComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EnvironmentEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
