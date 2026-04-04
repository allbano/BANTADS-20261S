import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FormEnderecoComponent } from './form-endereco.component';

describe('FormEnderecoComponent', () => {
  let component: FormEnderecoComponent;
  let fixture: ComponentFixture<FormEnderecoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormEnderecoComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(FormEnderecoComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
