import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExtractFeed } from './extract-feed';

describe('ExtractFeed', () => {
  let component: ExtractFeed;
  let fixture: ComponentFixture<ExtractFeed>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExtractFeed],
    }).compileComponents();

    fixture = TestBed.createComponent(ExtractFeed);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
