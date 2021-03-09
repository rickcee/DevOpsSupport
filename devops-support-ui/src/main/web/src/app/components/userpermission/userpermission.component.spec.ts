import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { UserpermissionComponent } from './userpermission.component';

describe('UserpermissionComponent', () => {
  let component: UserpermissionComponent;
  let fixture: ComponentFixture<UserpermissionComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ UserpermissionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserpermissionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
