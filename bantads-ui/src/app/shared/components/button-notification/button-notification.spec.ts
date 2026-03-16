import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ButtonNotification } from './button-notification';

describe('ButtonNotification', () => {
  let component: ButtonNotification;
  let fixture: ComponentFixture<ButtonNotification>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ButtonNotification],
    }).compileComponents();

    fixture = TestBed.createComponent(ButtonNotification);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
