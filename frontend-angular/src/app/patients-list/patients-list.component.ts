import {Component, input, Signal} from "@angular/core";
import {PatientModel} from '../patient.model';
import {PatientService} from '../patient.service';
import {PatientItemComponent} from '../patient-item/patient-item.component';

@Component({
  imports: [
    PatientItemComponent
  ],
  selector: "app-patients-list",
  styleUrl: "./patients-list.component.css",
  templateUrl: "./patients-list.component.html",
})
export class PatientsListComponent {

  constructor(private patientService: PatientService) {
  }

  getPatients(): Signal<PatientModel[]> {
    return this.patientService.getPatients();
  }

}
