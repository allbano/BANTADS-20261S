import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardGerente } from './dashboard-gerente';

describe('DashboardGerente', () => {
  let component: DashboardGerente;
  let fixture: ComponentFixture<DashboardGerente>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardGerente],
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardGerente);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
