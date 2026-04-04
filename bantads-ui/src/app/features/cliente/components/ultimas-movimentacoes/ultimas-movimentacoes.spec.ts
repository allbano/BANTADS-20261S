import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { UltimasMovimentacoes } from './ultimas-movimentacoes';

describe('UltimasMovimentacoes', () => {
  let component: UltimasMovimentacoes;
  let fixture: ComponentFixture<UltimasMovimentacoes>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UltimasMovimentacoes],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(UltimasMovimentacoes);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
