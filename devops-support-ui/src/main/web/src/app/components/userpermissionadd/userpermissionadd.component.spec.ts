import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { UserpermissionaddComponent } from './userpermissionadd.component';

describe('UserpermissionaddComponent', () => {
  let component: UserpermissionaddComponent;
  let fixture: ComponentFixture<UserpermissionaddComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ UserpermissionaddComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserpermissionaddComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
