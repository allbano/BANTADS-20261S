import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResumoConfirmacaoComponent } from './resumo-confirmacao.component';

describe('ResumoConfirmacaoComponent', () => {
  let component: ResumoConfirmacaoComponent;
  let fixture: ComponentFixture<ResumoConfirmacaoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResumoConfirmacaoComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ResumoConfirmacaoComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
