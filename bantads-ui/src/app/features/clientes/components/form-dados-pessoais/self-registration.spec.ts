import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelfRegistration } from './self-registration';

describe('SelfRegistration', () => {
  let component: SelfRegistration;
  let fixture: ComponentFixture<SelfRegistration>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SelfRegistration],
    }).compileComponents();

    fixture = TestBed.createComponent(SelfRegistration);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
