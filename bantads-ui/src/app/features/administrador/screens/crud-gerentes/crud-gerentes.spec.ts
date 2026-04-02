import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CrudGerentes } from './crud-gerentes';

describe('CrudGerentes', () => {
  let component: CrudGerentes;
  let fixture: ComponentFixture<CrudGerentes>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CrudGerentes],
    }).compileComponents();

    fixture = TestBed.createComponent(CrudGerentes);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
