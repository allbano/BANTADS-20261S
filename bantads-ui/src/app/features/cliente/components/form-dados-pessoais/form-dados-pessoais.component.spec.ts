import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FormDadosPessoaisComponent } from './form-dados-pessoais.component';

describe('FormDadosPessoaisComponent', () => {
  let component: FormDadosPessoaisComponent;
  let fixture: ComponentFixture<FormDadosPessoaisComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormDadosPessoaisComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(FormDadosPessoaisComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
