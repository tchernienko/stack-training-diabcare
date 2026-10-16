import { ComponentFixture, TestBed } from "@angular/core/testing";
import { PatientItemComponent } from "./patient-item.component";

describe("PatientItemComponent", () => {
  let component: PatientItemComponent;
  let fixture: ComponentFixture<PatientItemComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PatientItemComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(PatientItemComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
