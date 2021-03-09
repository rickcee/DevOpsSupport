import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RepositoryeditComponent } from './repositoryedit.component';

describe('RepositoryeditComponent', () => {
  let component: RepositoryeditComponent;
  let fixture: ComponentFixture<RepositoryeditComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RepositoryeditComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RepositoryeditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
